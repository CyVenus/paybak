export default function Footer() {
  return (
    <footer className="footer">
      <div className="wrap footer-inner">
        <div>
          <span className="logo logo--big">Paybak</span>
          <p>Split. Track. Settle.<br />No awkward money talks anymore.</p>
        </div>
        <nav>
          <a href="#how">How it works</a>
          <a href="#features">Features</a>
          <a href="#screens">Screens</a>
          <a href="#download">Download</a>
        </nav>
      </div>
      <div className="wrap footer-base">
        <span>© {new Date().getFullYear()} Paybak</span>
        <span className="hand">made for friends who hate awkward money chats ✦</span>
      </div>
    </footer>
  )
}
