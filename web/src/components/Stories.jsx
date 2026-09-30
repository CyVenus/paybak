import { Squiggle } from './Doodles'

const DINNER = [
  ['Arjun', '$50', '$250', 'Is owed $200', true],
  ['Bella', '$50', '$0', 'Owes Arjun $50'],
  ['Chen', '$50', '$0', 'Owes Arjun $50'],
  ['Dev', '$50', '$0', 'Owes Arjun $50'],
  ['Esha', '$50', '$0', 'Owes Arjun $50'],
]
const DRONE = [
  ['Sam', '$340', 'Is owed $210', true],
  ['Priya', '$120', 'Owes $10'],
  ['Leo', '$60', 'Owes $70'],
  ['Kiran', '$0', 'Owes $130'],
]

export default function Stories() {
  return (
    <section className="section" id="stories">
      <div className="wrap">
        <div className="section-head reveal">
          <span className="eyebrow">Real-life stories</span>
          <h2>Two tales, <span className="hl">zero arguments<Squiggle /></span></h2>
        </div>
        <div className="stories">
          <article className="receipt reveal">
            <header>
              <span className="receipt-kicker">Story 01 · 🍽</span>
              <h3>The Restaurant Bill</h3>
              <p>Five friends. One $250 bill. Arjun pays, splits equally, due Sunday.</p>
            </header>
            <table>
              <thead><tr><th>Person</th><th>Share</th><th>Paid</th><th>Result</th></tr></thead>
              <tbody>
                {DINNER.map(([n, s, p, r, pos]) => (
                  <tr key={n}><td>{n}</td><td>{s}</td><td>{p}</td><td className={pos ? 'pos' : ''}>{r}</td></tr>
                ))}
              </tbody>
            </table>
            <footer>
              Bella pays $50 by UPI → taps <b>Record Payment</b> → Arjun confirms. Bella is all settled ✓.
              Chen, Dev and Esha get a friendly nudge on Saturday.
            </footer>
          </article>

          <article className="receipt reveal">
            <header>
              <span className="receipt-kicker">Story 02 · 🚁</span>
              <h3>The Drone Project</h3>
              <p>Four friends. A $600 budget. $520 spent across five parts — fair share $130 each.</p>
            </header>
            <table>
              <thead><tr><th>Member</th><th>Paid</th><th>Result</th></tr></thead>
              <tbody>
                {DRONE.map(([n, p, r, pos]) => (
                  <tr key={n}><td>{n}</td><td>{p}</td><td className={pos ? 'pos' : ''}>{r}</td></tr>
                ))}
              </tbody>
            </table>
            <footer>
              Camera added later? Shares update automatically. When the drone flies, the team settles up and
              the project is archived as a permanent record.
            </footer>
          </article>
        </div>
      </div>
    </section>
  )
}
