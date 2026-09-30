import { useMemo, useState } from 'react'
import { Avatar, Squiggle } from './Doodles'

const CURRENCIES = { USD: '$', EUR: '€', GBP: '£', INR: '₹' }
const START = [
  { name: 'You', weight: 1, pct: 25 },
  { name: 'Aditi', weight: 1, pct: 25 },
  { name: 'Rohan', weight: 1, pct: 25 },
  { name: 'Meera', weight: 1, pct: 25 },
]
const EXTRA = ['Kabir', 'Neha', 'Sam', 'Priya', 'Leo', 'Kiran']

// Split `total` cents by `weights`; leftover cents go one each to the first
// people so the shares always add up exactly to the total.
function splitCents(total, weights) {
  const sum = weights.reduce((a, b) => a + b, 0)
  if (!sum) return weights.map(() => 0)
  const raw = weights.map((w) => (total * w) / sum)
  const base = raw.map(Math.floor)
  let left = total - base.reduce((a, b) => a + b, 0)
  const order = raw.map((r, i) => [r - Math.floor(r), i]).sort((a, b) => b[0] - a[0])
  for (let k = 0; left > 0; k++, left--) base[order[k % order.length][1]] += 1
  return base
}

export default function SplitDemo() {
  const [amount, setAmount] = useState('124')
  const [cur, setCur] = useState('USD')
  const [mode, setMode] = useState('equal')
  const [people, setPeople] = useState(START)

  const sym = CURRENCIES[cur]
  const totalCents = Math.max(0, Math.round((parseFloat(amount) || 0) * 100))
  const pctSum = people.reduce((a, p) => a + (Number(p.pct) || 0), 0)

  const shares = useMemo(() => {
    const w = people.map((p) =>
      mode === 'equal' ? 1 : mode === 'shares' ? Number(p.weight) || 0 : Number(p.pct) || 0,
    )
    return splitCents(totalCents, w)
  }, [people, mode, totalCents])

  const fmt = (c) =>
    sym + (c / 100).toLocaleString('en-US', { minimumFractionDigits: c % 100 ? 2 : 0, maximumFractionDigits: 2 })

  const update = (i, key, val) =>
    setPeople((ps) => ps.map((p, j) => (j === i ? { ...p, [key]: val } : p)))

  const addPerson = () => {
    const name = EXTRA.find((n) => !people.some((p) => p.name === n))
    if (name) setPeople([...people, { name, weight: 1, pct: 0 }])
  }
  const removePerson = (i) => people.length > 2 && setPeople(people.filter((_, j) => j !== i))

  const invalid = mode === 'percent' && Math.abs(pctSum - 100) > 0.001
  const owedToYou = totalCents - shares[0]

  return (
    <section className="section section--ink" id="split">
      <div className="wrap split-grid">
        <div className="split-copy reveal">
          <span className="eyebrow eyebrow--light">Try it right here</span>
          <h2>Split a bill in <span className="hl">seconds<Squiggle /></span></h2>
          <p className="lead">
            Pick an amount, choose how to split, and watch every share land to the
            exact cent. Leftover cents are assigned fairly, so totals always match.
          </p>
          <ul className="ticks">
            <li>Equally, by percentage, or by shares</li>
            <li>Every expense keeps its original currency</li>
            <li>Can't save until the math adds up</li>
          </ul>
        </div>

        <div className="split-card reveal">
          <div className="split-top">
            <label className="field">
              <span>Amount</span>
              <div className="amount">
                <em>{sym}</em>
                <input
                  inputMode="decimal"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value.replace(/[^\d.]/g, ''))}
                  aria-label="Amount"
                />
              </div>
            </label>
            <label className="field field--cur">
              <span>Currency</span>
              <select value={cur} onChange={(e) => setCur(e.target.value)}>
                {Object.keys(CURRENCIES).map((c) => <option key={c}>{c}</option>)}
              </select>
            </label>
          </div>

          <div className="seg" role="tablist">
            {[['equal', 'Equally'], ['percent', 'By %'], ['shares', 'By shares']].map(([k, l]) => (
              <button key={k} role="tab" aria-selected={mode === k} className={mode === k ? 'on' : ''} onClick={() => setMode(k)}>
                {l}
              </button>
            ))}
          </div>

          <ul className="split-list">
            {people.map((p, i) => (
              <li key={p.name}>
                <Avatar v={i} size={34} />
                <span className="split-name">
                  {p.name}
                  {i === 0 && <small>paid the bill</small>}
                </span>
                {mode === 'percent' && (
                  <input className="mini-in" inputMode="decimal" value={p.pct} onChange={(e) => update(i, 'pct', e.target.value.replace(/[^\d.]/g, ''))} aria-label={`${p.name} percent`} />
                )}
                {mode === 'shares' && (
                  <span className="stepper">
                    <button onClick={() => update(i, 'weight', Math.max(0, p.weight - 1))} aria-label="less">−</button>
                    <b>{p.weight}</b>
                    <button onClick={() => update(i, 'weight', p.weight + 1)} aria-label="more">+</button>
                  </span>
                )}
                <strong className="split-amt" key={shares[i] || 0}>{fmt(shares[i] || 0)}</strong>
                {people.length > 2 && i > 0 && (
                  <button className="x" onClick={() => removePerson(i)} aria-label={`Remove ${p.name}`}>×</button>
                )}
              </li>
            ))}
          </ul>

          {people.length < START.length + EXTRA.length && (
            <button className="add-person" onClick={addPerson}>+ Add friend</button>
          )}

          <div className={`split-foot ${invalid ? 'is-bad' : ''}`}>
            {invalid ? (
              <span>Percentages add up to {pctSum}% — needs to be 100%</span>
            ) : (
              <>
                <span>You are owed</span>
                <strong key={owedToYou}>{fmt(owedToYou)}</strong>
              </>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}
