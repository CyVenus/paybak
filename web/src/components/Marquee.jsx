const WORDS = ['Dinners', 'Rent', 'Goa Trip', 'Build a Drone', 'Netflix', 'Wi-Fi', 'Movie night', 'Groceries', 'Hackathon', 'Road trip', 'Electricity', 'Birthday gift']

export default function Marquee() {
  const row = [...WORDS, ...WORDS]
  return (
    <div className="marquee" aria-hidden="true">
      <div className="marquee-track">
        {row.map((w, i) => (
          <span key={i}>{w}<em>✦</em></span>
        ))}
      </div>
    </div>
  )
}
