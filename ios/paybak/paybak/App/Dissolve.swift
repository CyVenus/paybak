import QuartzCore
import SwiftUI
import UIKit

/// Performs `change` with Figma's DISSOLVE instead of the usual navigation slide (Welcome's Skip →
/// Get Started). SwiftUI's `.crossFade` navigation transition applies only to sheets and covers, so
/// the key window cross-fades from its current look (a fade `CATransition`) while the change, such
/// as a stack push, happens without animation.
func withDissolve(duration: TimeInterval, _ change: () -> Void) {
    let fade = CATransition()
    fade.type = .fade
    fade.duration = duration
    fade.timingFunction = CAMediaTimingFunction(name: .easeInEaseOut)
    let keyWindow = UIApplication.shared.connectedScenes
        .compactMap { ($0 as? UIWindowScene)?.keyWindow }
        .first
    keyWindow?.layer.add(fade, forKey: kCATransition)

    var transaction = Transaction()
    transaction.disablesAnimations = true
    withTransaction(transaction, change)
}
