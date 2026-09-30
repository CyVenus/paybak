import SwiftUI

/// Which way money goes, as rows and cards show it. Amount strings never carry the sign: Owed adds
/// "+" in black, Owe adds "−" (U+2212) in gray, both in Amount/Medium.
enum PBAmountDirection {
    case owed
    case owe

    var sign: String {
        switch self {
        case .owed: "+"
        case .owe: "\u{2212}"
        }
    }

    var color: Color {
        switch self {
        case .owed: PBColor.textPrimary
        case .owe: PBColor.textSecondary
        }
    }
}

/// "+₹700" / "−₹700" in Amount/Medium: the sign and the amount share the style and colour, gap 0.
struct PBSignedAmount: View {
    let amount: String
    let direction: PBAmountDirection

    init(_ amount: String, direction: PBAmountDirection) {
        self.amount = amount
        self.direction = direction
    }

    var body: some View {
        Text(direction.sign + amount)
            .textStyle(.amountMedium)
            .foregroundStyle(direction.color)
            .lineLimit(1)
    }
}

#Preview("PBSignedAmount") {
    VStack(spacing: PBSpace.s8) {
        PBSignedAmount("₹700", direction: .owed)
        PBSignedAmount("₹700", direction: .owe)
    }
}
