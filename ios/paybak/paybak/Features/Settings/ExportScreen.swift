import SwiftUI

/// Export records (screens-settings §9, Pro): PDF or CSV, a range, and the groups to include. A
/// group starts ticked iff it has records in the range (recomputed when the range changes). Export
/// builds the file on the device and opens the share sheet with it.
struct ExportScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    @State private var format = 0
    @State private var range = ExportRange.thisMonth
    @State private var ticked: Set<String>?
    @State private var share: ShareItem?

    private var interval: (start: LocalDay, end: LocalDay) { ledgerStore.books.exportInterval(range) }
    private var rows: [ExportGroupRow] { ledgerStore.books.exportGroups(from: interval.start, to: interval.end) }
    private var selection: Set<String> { ticked ?? defaultTicks }
    private var defaultTicks: Set<String> { Set(rows.filter(\.hasRecords).map(\.id)) }
    private var allTicked: Bool { rows.allSatisfy { selection.contains($0.id) } }

    var body: some View {
        let rows = rows
        SettingsScaffold(title: "Export records", testIDPrefix: "privacyExport") {
            SettingsSection(title: "Format") {
                PBSegmentedControl(options: ["PDF", "CSV"], selection: $format, testIDPrefix: "privacyExport.format")
            }
            SettingsSection(title: "Range") {
                HStack(spacing: PBSpace.s8) {
                    ForEach(ExportRange.allCases, id: \.self) { option in
                        PBCategoryChip(option.label, isSelected: option == range) {
                            range = option
                            ticked = nil
                        }
                        .accessibilityIdentifier("privacyExport.range.\(option.rawValue)")
                    }
                }
                SettingsFooter(Format.exportRange(interval.start, interval.end))
                    .accessibilityIdentifier("privacyExport.rangeLabel")
            }
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSectionHeader("Groups", actionTitle: allTicked ? "Deselect all" : "Select all") {
                    ticked = allTicked ? [] : Set(rows.map(\.id))
                }
                .accessibilityIdentifier("privacyExport.selectAll")
                VStack(spacing: 0) {
                    ForEach(rows) { row in
                        PBSettingRow(row.name, trailing: selection.contains(row.id) ? .check : .unchecked,
                                     showsDivider: row.id != rows.last?.id) { toggle(row.id) }
                            .accessibilityIdentifier("privacyExport.group.\(row.id)")
                    }
                }
                .pbCard(padding: 0)
                SettingsFooter("Includes expenses, payments and loans, with each person’s share.")
            }
        } bottom: {
            PBButton("Export", fillsWidth: true, action: export)
                .disabled(selection.isEmpty)
                .accessibilityIdentifier("privacyExport.export")
                .padding(.horizontal, PBLayout.screenMargin)
                .background(PBColor.bgPrimary)
        }
        .systemShare(item: $share)
    }

    private func toggle(_ id: String) {
        var set = selection
        if set.contains(id) { set.remove(id) } else { set.insert(id) }
        ticked = set
    }

    /// Pro only: a lapsed entitlement goes to the paywall, which comes back here.
    private func export() {
        guard ledgerStore.isPro else {
            router.open(.paywall(continueTo: nil))
            return
        }
        do {
            let url = try Exporter.export(ledgerStore.books, from: interval.start, to: interval.end, groups: selection,
                                          format: format == 0 ? .pdf : .csv)
            share = ShareItem(url: url)
        } catch {
            router.toast("Couldn’t export your records")
        }
    }
}

#Preview("ExportScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-export")!)
    ExportScreen()
        .environment(AppRouter())
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-export.json")), profileStore: profileStore))
}
