export default function NotABank() {
  return (
    <section className="section section--tight">
      <div className="wrap">
        <div className="notabank reveal">
          <div className="stamp">Track<br />only</div>
          <div>
            <h3>Paybak doesn’t move money. <span className="hand">On purpose.</span></h3>
            <p>
              Pay each other however you like — cash, UPI, bank transfer, card — then record it in Paybak.
              The receiver confirms, and the ledger stays correct. No wallets, no fees, no interest.
            </p>
            <div className="methods">
              {['Cash', 'UPI', 'Bank transfer', 'Card', 'Other'].map((m) => <span key={m}>{m}</span>)}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
