import PhotosUI
import SwiftUI
import UIKit
import os

/// Setup 1 — Name & photo (screens-setup.md §1): pick one of five line-art avatars or a photo from
/// the system picker, and enter a name. Continue (enabled once the name isn't blank) saves both and
/// goes to Setup 2; with no avatar chosen the initials are the fallback. The name field is focused on
/// arrival (not when Back from Setup 2 returns here) and the CTA rides 12 pt above the keyboard. Back
/// returns to Verify or Get Started.
struct SetupNameScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var name = ""
    @State private var avatar: UserProfile.Avatar?
    /// A photo picked here and not saved yet: Continue saves it.
    @State private var pickedPhoto: UIImage?
    @State private var photoItem: PhotosPickerItem?
    @State private var isPickingPhoto = false
    @State private var hasAppeared = false
    @FocusState private var isNameFocused: Bool

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Setup")

    private var trimmedName: String { name.trimmingCharacters(in: .whitespacesAndNewlines) }

    var body: some View {
        VStack(spacing: 0) {
            PBSetupHeader(step: 1, onBack: router.pop)
                .padding(.horizontal, PBLayout.screenMargin)
            // With the keyboard up everything only just fits on the Figma-sized screen, so on shorter
            // screens or with larger text the content scrolls instead of squeezing its copy.
            ScrollView {
                content
                    .padding(.horizontal, PBLayout.screenMargin)
                    .padding(.bottom, PBSpace.s24)
            }
            .scrollBounceBehavior(.basedOnSize)
            .scrollDismissesKeyboard(.interactively)
            .safeAreaInset(edge: .bottom, spacing: 0) {
                PBButton("Continue", fillsWidth: true, action: next)
                    .disabled(trimmedName.isEmpty)
                    .accessibilityIdentifier("setup1.continue")
                    .padding(.horizontal, PBLayout.screenMargin)
                    .keyboardGap()
            }
        }
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .screenIdentifier(.setup1)
        .photosPicker(isPresented: $isPickingPhoto, selection: $photoItem, matching: .images)
        .task(id: photoItem) { await loadPickedPhoto() }
        .onAppear {
            // Only on arrival. Raising the keyboard while Back reveals this screen would leave
            // Continue under it: SwiftUI skips its keyboard avoidance during the pop transition.
            guard !hasAppeared else { return }
            hasAppeared = true
            loadProfile()
            isNameFocused = true
        }
    }

    private var content: some View {
        VStack(alignment: .leading, spacing: 0) {
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                Text("What’s your name?")
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                FigmaWrappedText("Friends see your name and picture on shared\nexpenses.", style: .body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .padding(.top, PBSpace.s24)
            AvatarPicker(selection: $avatar, photo: pickedPhoto ?? profileStore.photo, onCamera: cameraTapped)
                .padding(.top, PBSpace.s24)
            PBTextField("Name", text: $name, prompt: "Your name", focus: $isNameFocused)
                .textContentType(.name)
                .textInputAutocapitalization(.words)
                .autocorrectionDisabled()
                .submitLabel(.continue)
                .onSubmit(next)
                .accessibilityIdentifier("setup1.name")
                .padding(.top, PBSpace.s20)
        }
    }

    /// Starts from what's saved (coming back later, or the debug seed).
    private func loadProfile() {
        name = profileStore.profile.name
        avatar = profileStore.profile.avatar
    }

    /// The camera tile opens the picker; once it shows a photo, the first tap selects the photo and
    /// a tap on the selected photo picks a new one.
    private func cameraTapped() {
        if avatar != .photo, pickedPhoto ?? profileStore.photo != nil {
            avatar = .photo
        } else {
            isPickingPhoto = true
        }
    }

    private func loadPickedPhoto() async {
        guard let photoItem else { return }
        do {
            guard let data = try await photoItem.loadTransferable(type: Data.self),
                  let image = UIImage(data: data)
            else { return }
            pickedPhoto = ProfileStore.avatarPhoto(from: image)
            avatar = .photo
        } catch {
            Self.log.error("Could not load the picked photo: \(String(describing: error), privacy: .public)")
        }
        self.photoItem = nil
    }

    private func next() {
        guard !trimmedName.isEmpty else {
            isNameFocused = true
            return
        }
        var chosen = avatar
        if chosen == .photo, let pickedPhoto {
            do {
                try profileStore.savePhoto(pickedPhoto)
                self.pickedPhoto = nil
            } catch {
                Self.log.error("Could not save the photo: \(String(describing: error), privacy: .public)")
                chosen = nil
            }
        }
        profileStore.update {
            $0.name = trimmedName
            $0.avatar = chosen
        }
        isNameFocused = false
        router.push(.setup2)
    }
}

/// The five preset avatars and the camera tile, spread across the width (space-between). On
/// screens too narrow for six 56 pt options with 4 pt gaps, the options shrink.
private struct AvatarPicker: View {
    @Binding var selection: UserProfile.Avatar?
    /// The photo the camera tile shows, if one was picked or saved.
    let photo: UIImage?
    let onCamera: () -> Void

    @State private var width: CGFloat = 362

    private var diameter: CGFloat {
        let count = CGFloat(PBPeepHead.presets.count + 1)
        return min(PBSize.avatarLg, (width - (count - 1) * PBSpace.s4) / count)
    }

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(PBPeepHead.presets.enumerated()), id: \.offset) { index, head in
                PBAvatarOption(kind: .art(head), isSelected: selection == .preset(index), diameter: diameter) {
                    selection = .preset(index)
                }
                .accessibilityIdentifier("setup1.avatar.\(index)")
                Spacer(minLength: 0)
            }
            PBAvatarOption(
                kind: photo.map { .photo(Image(uiImage: $0)) } ?? .upload,
                isSelected: selection == .photo,
                diameter: diameter,
                action: onCamera
            )
            .accessibilityIdentifier("setup1.camera")
        }
        .frame(maxWidth: .infinity)
        .onGeometryChange(for: CGFloat.self, of: \.size.width) { width = $0 }
    }
}

#Preview("SetupNameScreen") {
    SetupNameScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
