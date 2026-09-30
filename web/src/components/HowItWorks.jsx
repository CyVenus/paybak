import { Squiggle } from './Doodles'

const STEPS = [
  {
    n: '1/3',
    t: 'Add expenses together',
    d: 'Log any expense once and split it your way — equally, by amount, or by percentage. Under 15 seconds.',
    art: (
      <div className="how-art how-art--receipt">
        <div className="receipt-bubble">$124</div>
        <div className="receipt-lines"><i /><i /><i /><i /></div>
      </div>
    ),
  },
  {
    n: '2/3',
    t: 'Keep track automatically',
    d: 'See who owes whom, in which currency, and by when. Balances update the second anything changes.',
    art: (
      <div className="how-art">
        <div className="mini-chip mini-chip--ink">You are owed <b>$80</b></div>
        <div className="mini-chip mini-chip--hatch">You owe <b>$20</b></div>
      </div>
    ),
  },
  {
    n: '3/3',
    t: 'Settle without awkwardness',
    d: 'Record payments made via cash, UPI or bank. Friendly reminders do the chasing for you.',
    art: (
      <div className="how-art">
        <div className="high-five">✋<span>✦</span>🤚</div>
      </div>
    ),
  },
]

export default function HowItWorks() {
  return (
    <section className="section" id="how">
      <div className="wrap">
        <div className="section-head reveal">
          <span className="eyebrow">How it works</span>
          <h2>Three steps to <span className="hl">zero drama<Squiggle /></span></h2>
        </div>
        <div className="how-grid">
          {STEPS.map((s, i) => (
            <article className="card how-card reveal" style={{ transitionDelay: `${i * 90}ms` }} key={s.n}>
              <span className="how-n">{s.n}</span>
              {s.art}
              <h3>{s.t}</h3>
              <p>{s.d}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}
