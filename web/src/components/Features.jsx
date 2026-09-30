import { useState } from 'react'
import { Squiggle, Avatar, Check } from './Doodles'

function Simplify() {
  const [on, setOn] = useState(true)
  return (
    <div className="simp">
      <button className={`toggle ${on ? 'on' : ''}`} onClick={() => setOn(!on)} aria-pressed={on}>
        <i /> Simplify {on ? 'on' : 'off'}
      </button>
      <div className="simp-row">
        <span className="node">Ana</span>
        <span className={`edge ${on ? 'is-gone' : ''}`}>$100 →</span>
        <span className={`node ${on ? 'is-dim' : ''}`}>Ben</span>
        <span className={`edge ${on ? 'is-gone' : ''}`}>$100 →</span>
        <span className="node">Cal</span>
      </div>
      <div className={`simp-direct ${on ? 'is-on' : ''}`}>Ana pays Cal $100 — one payment instead of two</div>
    </div>
  )
}

const SMALL = [
  ['⌗', 'Groups', 'Flat 302, Goa Trip, College Gang — each with its own members, balances and default currency.'],
  ['⏰', 'Due dates & reminders', 'Auto nudges 2 days before, on the day, and when overdue. Mute per friend anytime.'],
  ['⎌', 'History you can trust', '“Bella changed the amount from $40 to $45.” Every edit logged, deletes restorable for 30 days.'],
  ['⚑', 'Comments & disputes', '“Wait, I didn’t have dessert!” Flag an expense and sort it out inside the app.'],
  ['⟳', 'Recurring expenses', 'Rent, Wi-Fi, Netflix, gym — auto-created every cycle, with drafts for variable bills.'],
  ['▦', 'Receipt scanning', 'Snap the bill. Paybak reads items, tax and tip — tap each dish to assign it.'],
  ['◔', 'Spending insights', 'Monthly trends, categories, per-friend and per-project breakdowns.'],
  ['⌘', 'Guest friends', 'Add anyone, even if they’re not on Paybak. Their history links up when they join.'],
]

export default function Features() {
  return (
    <section className="section" id="features">
      <div className="wrap">
        <div className="section-head reveal">
          <span className="eyebrow">Everything shared money needs</span>
          <h2>Small app. <span className="hl">Big peace of mind.<Squiggle /></span></h2>
        </div>

        <div className="bento">
          <article className="card b-wide reveal">
            <span className="tag">Smart splits</span>
            <h3>Split it any way you like</h3>
            <p>Equally, exact amounts, percentages, shares, or itemized — with multiple payers when two people chip in.</p>
            <div className="chips">
              <b>Equally</b><span>Exact</span><span>Percent</span><span>Shares</span><span>Itemized</span>
            </div>
          </article>

          <article className="card card--ink b-tall reveal">
            <span className="tag tag--light">Projects</span>
            <h3>Build a Drone 🚁</h3>
            <p>Goal-based groups with a budget and components. Add a part, and everyone’s share recalculates.</p>
            <div className="proj">
              <div className="proj-head"><span>$520 spent</span><span>of $600</span></div>
              <div className="bar bar--light"><i style={{ width: '87%' }} /></div>
              {[['Frame', 'Sam', '$80', 'Done'], ['4 Motors', 'Sam', '$160', 'Done'], ['Flight Controller', 'Priya', '$120', 'Bought'], ['Battery', 'Leo', '$60', 'Bought'], ['Camera', 'Sam', '$100', 'Planned']].map(([n, by, c, st]) => (
                <div className="proj-row" key={n}>
                  <span>{n}<small>{by}</small></span>
                  <em>{st}</em>
                  <b>{c}</b>
                </div>
              ))}
            </div>
          </article>

          <article className="card reveal">
            <span className="tag">Debt simplification</span>
            <h3>Fewer payments</h3>
            <Simplify />
          </article>

          <article className="card reveal">
            <span className="tag">Multi-currency</span>
            <h3>$, €, ₹ — no problem</h3>
            <p>Rates are locked at the time of the expense, so balances never drift.</p>
            <div className="fx">
              <span>€60 hotel</span><span className="fx-eq">=</span><span>$65.40</span>
              <small>@ 1.09 · saved 12 Sep</small>
            </div>
          </article>

          <article className="card reveal">
            <span className="tag">Loans / IOUs</span>
            <h3>“I lent Dev $300”</h3>
            <div className="loan">
              <div><small>Original</small><b>$300</b></div>
              <div><small>Paid</small><b>$200</b></div>
              <div><small>Left</small><b>$100</b></div>
            </div>
            <div className="installments">
              <span className="on"><Check /></span><span className="on"><Check /></span><span>3</span>
              <small>$100 / month</small>
            </div>
          </article>

          <article className="card b-wide reveal">
            <span className="tag">AI assistant</span>
            <h3>Just ask</h3>
            <div className="chat">
              <div className="bubble bubble--me">Add $60 for cab, split with Chen and Esha</div>
              <div className="bubble">
                <Avatar v={4} size={24} />
                <span>Got it — $60 cab, $20 each. You’re owed $40. <b>Save it?</b></span>
              </div>
              <div className="chat-actions"><span className="pill-ink">Confirm</span><span className="pill-line">Edit</span></div>
            </div>
            <p className="fine">Never saves anything without your confirmation. Only sees your own data.</p>
          </article>
        </div>

        <div className="small-grid">
          {SMALL.map(([ic, t, d], i) => (
            <div className="small reveal" style={{ transitionDelay: `${(i % 4) * 70}ms` }} key={t}>
              <span className="small-ic">{ic}</span>
              <h4>{t}</h4>
              <p>{d}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
