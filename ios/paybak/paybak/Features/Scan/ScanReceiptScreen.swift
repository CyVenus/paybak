import PhotosUI
import SwiftUI

/// Scan receipt (screens-insights-ai §4): the camera (live, or the bundled receipt on the simulator),
/// then Check receipt and Assign items pushed inside this modal, ending in the Add expense form it
/// was opened from, prefilled (`.receipt(result)`). Reading is Pro; a free user's photo is only
/// attached. A request carrying a scanned expense's items opens straight on Assign items. Every page's
/// test root is `screen.scanReceipt`, with `scanReceipt.state.<page>`.
struct ScanReceiptScreen: View {
    let request: ScanRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @State private var camera = CameraSource()
    @State private var photo: UIImage?
    /// The simulated feed's shot: Check receipt shows the receipt art as its thumbnail.
    @State private var isSimulatedShot = false
    @State private var review: ReceiptReview?
    @State private var isReading = false
    @State private var isUnreadable = false
    @State private var showsReview = false
    @State private var showsAssign = false
    @State private var pickedPhoto: PhotosPickerItem?
    /// The people besides you, once Assign items' "Add" changed them.
    @State private var others: [PersonID]?
    @State private var peopleRequest = RecordID.make()
    @State private var showsPeople = false

    var body: some View {
        if let itemized = request.itemized {
            ScanAssignPage(review: $review, people: people, onBack: close, onContinue: finish)
                .onAppear {
                    review = review ?? ReceiptReview(reassigning: itemized, among: people, on: ledgerStore.books.today)
                }
        } else {
            scan
        }
    }

    private var scan: some View {
        ScanCameraView(camera: camera, isReading: isReading, pickedPhoto: $pickedPhoto, onClose: close) {
            Task { await capture() }
        }
        // The light status bar on the dark camera; the pushed pages are light again (the app is
        // light only, and `nil` wouldn't undo the dark glass).
        .preferredColorScheme(showsReview ? .light : .dark)
        .task { await camera.start() }
        .onDisappear { camera.stop() }
        .onChange(of: pickedPhoto) { Task { await upload() } }
        .navigationDestination(isPresented: $showsReview) {
            ScanReviewPage(review: $review, photo: isSimulatedShot ? nil : photo, isUnreadable: isUnreadable, onRetake: retake, onAttach: attachOnly) {
                showsAssign = true
            }
            .navigationDestination(isPresented: $showsAssign) {
                assignPage
            }
        }
        .scanState("camera")
        .onStartScreen([.scanReview, .scanAssign]) { screen in
            Task {
                isSimulatedShot = true
                await read(CameraSource.simulatedPhoto)
                guard screen == .scanAssign else { return }
                review?.assignment = Self.drawnAssignment
                showsAssign = true
            }
        }
    }

    /// Assign items; scanned from a form with nobody but you, "Add" picks the people (Split with,
    /// pushed inside this flow).
    private var assignPage: some View {
        ScanAssignPage(review: $review, people: people, addsPeople: startsAlone, onAddPeople: { showsPeople = true },
                       onBack: { showsAssign = false }, onContinue: finish)
            .navigationDestination(isPresented: $showsPeople) {
                PeoplePickerScreen(request: PeoplePickRequest(id: peopleRequest, selected: Array(people.dropFirst())),
                                   onClose: { showsPeople = false })
                    .navigationBarHiddenKeepingSwipeBack()
            }
            .onRouteResult(peopleRequest) { result in
                if case .people(let ids) = result { setOthers(ids) }
            }
    }

    /// The people on the expense (you first), as the form passed them or as picked since.
    private var people: [PersonID] {
        [Person.me] + (others ?? request.people.filter { $0 != Person.me })
    }

    /// The form had nobody but you on the expense.
    private var startsAlone: Bool {
        request.people.allSatisfy { $0 == Person.me }
    }

    /// You and the picked people are on the expense now; items lose anyone dropped.
    private func setOthers(_ ids: [PersonID]) {
        var next: [PersonID] = []
        for id in ids where id != Person.me && !next.contains(id) {
            next.append(id)
        }
        others = next
        let everyone = Set([Person.me] + next)
        if var current = review {
            current.assignment = current.assignment.map { $0.filter(everyone.contains) }
            review = current
        }
    }

    // MARK: Reading

    private func capture() async {
        isSimulatedShot = camera.isSimulated
        await take(try? await camera.capture())
    }

    private func upload() async {
        guard let item = pickedPhoto else { return }
        pickedPhoto = nil
        let data = try? await item.loadTransferable(type: Data.self)
        isSimulatedShot = false
        await take(data.flatMap(UIImage.init(data:)))
    }

    /// Pro reads the photo; free attaches it to the expense as it is (§4.2).
    private func take(_ image: UIImage?) async {
        guard let image else { return }
        photo = image
        if ledgerStore.isPro {
            await read(image)
        } else {
            attachOnly()
        }
    }

    /// Reads the photo, then shows Check receipt (or its "Couldn’t read this receipt" notice).
    private func read(_ image: UIImage?) async {
        guard let image, !isReading else { return }
        photo = image
        isReading = true
        defer { isReading = false }
        let books = ledgerStore.books
        if let scan = try? await ReceiptReader.read(image, currency: books.defaultCurrency) {
            review = ReceiptReview(scan, today: books.today)
            isUnreadable = false
        } else {
            review = nil
            isUnreadable = true
        }
        showsReview = true
    }

    // MARK: Results

    private func retake() {
        showsReview = false
        review = nil
    }

    /// "Attach photo": back to the form with the photo only.
    private func attachOnly() {
        guard let photo, let name = try? PhotoFiles.save(photo) else { return }
        var draft = ExpenseDraft(currency: ledgerStore.books.defaultCurrency, date: ledgerStore.books.today)
        draft.receipt = Receipt(photo: name, asset: nil, addedBy: Person.me, addedAt: ledgerStore.clock.now)
        router.complete(request.id, with: .receipt(ReceiptResult(draft: draft, photo: name)))
    }

    /// The itemized draft back to the form: a new scan brings its photo, a reassignment (no photo)
    /// keeps the form's receipt.
    private func finish() {
        guard let review else { return }
        let name = photo.flatMap { try? PhotoFiles.save($0) }
        let receipt = name.map { Receipt(photo: $0, asset: nil, addedBy: Person.me, addedAt: ledgerStore.clock.now) }
        let draft = review.expenseDraft(order: people, currency: ledgerStore.books.defaultCurrency, receipt: receipt)
        router.complete(request.id, with: .receipt(ReceiptResult(draft: draft, photo: name)))
    }

    private func close() {
        router.dismissModal()
    }

    /// The assignment Figma draws (scanAssign): Dev · Esha · You · You · all three · all three.
    private static let drawnAssignment: [Set<PersonID>] = [
        ["p-dev"], ["p-esha"], [Person.me], [Person.me], [Person.me, "p-esha", "p-dev"], [Person.me, "p-esha", "p-dev"],
    ]
}

extension View {
    /// The scan flow's test root and its page (`scanReceipt.state.<page>`).
    func scanState(_ page: String) -> some View {
        overlay(alignment: .topLeading) {
            Color.clear
                .frame(width: 1, height: 1)
                .accessibilityElement()
                .accessibilityLabel(page)
                .accessibilityIdentifier("scanReceipt.state.\(page)")
        }
        .routeTestRoot("scanReceipt")
    }
}
