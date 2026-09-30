export default function Phone({ children, className = '', label }) {
  return (
    <div className={`phone ${className}`} role="img" aria-label={label}>
      <div className="phone-screen">
        <div className="phone-status">
          <span>9:41</span>
          <span className="phone-notch" />
          <span className="phone-icons">
            <i /><i /><i /><b />
          </span>
        </div>
        <div className="phone-body">{children}</div>
      </div>
    </div>
  )
}
