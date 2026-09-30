import { Avatar, Check } from '../Doodles'

const Back = () => <span className="s-back">‹</span>
const Dots = () => <span className="s-dots">⋮</span>

export function HomeScreen() {
  const rows = [
    ['Dinner at Toscano', 'You paid · Friends', '+$32', '🍽'],
    ['Metro Recharge', 'You owe · Roommates', '−$6', '🚇'],
    ['Movie Tickets', 'You paid · Friends', '+$18', '🎬'],
    ['Flight Tickets', 'You owe · Goa Trip', '−$120', '✈'],
  ]
  return (
    <div className="s">
      <div className="s-row s-between">
        <div className="s-row">
          <Avatar v={0} size={30} />
          <div>
            <div className="s-mute">Hello,</div>
            <div className="s-h3">Shreyas 👋</div>
          </div>
        </div>
        <span className="s-mute">⌕ &nbsp;🔔</span>
      </div>
      <div className="s-tabs"><b>Overview</b><span>Groups</span></div>
      <div className="s-grid2">
        <div className="s-stat s-stat--ink">
          <span>You are owed</span>
          <strong>$243</strong>
        </div>
        <div className="s-stat s-stat--hatch">
          <span>You owe</span>
          <strong>$52</strong>
        </div>
      </div>
      <div className="s-row s-between"><span className="s-h4">Recent activity</span><span className="s-mute">See all ›</span></div>
      <div className="s-list">
        {rows.map(([t, sub, amt, ic]) => (
          <div className="s-item" key={t}>
            <span className="s-ic">{ic}</span>
            <div className="s-grow">
              <div className="s-t">{t}</div>
              <div className="s-mute">{sub}</div>
            </div>
            <b className={amt.startsWith('+') ? 's-pos' : 's-neg'}>{amt}</b>
          </div>
        ))}
      </div>
      <div className="s-tabbar">
        <span>⌂<small>Home</small></span>
        <span>◎<small>Groups</small></span>
        <span className="s-fab">+</span>
        <span>♡<small>Activity</small></span>
        <span>◯<small>Profile</small></span>
      </div>
    </div>
  )
}

export function AddExpenseScreen() {
  return (
    <div className="s">
      <div className="s-row s-between"><span>✕</span><span className="s-h4">Add Expense</span><span /></div>
      <div className="s-tabs"><b>Expense</b><span>Loan</span></div>
      <label className="s-label">Title</label>
      <div className="s-input">Dinner at Toscano</div>
      <label className="s-label">Amount</label>
      <div className="s-row">
        <div className="s-input s-grow s-big">$ 124</div>
        <div className="s-input">USD ▾</div>
      </div>
      <label className="s-label">Split type</label>
      <div className="s-chips">
        <b>Equally</b><span>By amount</span><span>By %</span>
      </div>
      <label className="s-label">With</label>
      <div className="s-row s-people">
        {['You', 'Aditi', 'Rohan', 'Meera'].map((n, i) => (
          <span key={n}><Avatar v={i} size={28} /><small>{n}</small></span>
        ))}
        <span><span className="s-add">+</span><small>Add</small></span>
      </div>
      <label className="s-label">Date</label>
      <div className="s-input">📅 24 Sep 2026</div>
      <div className="s-btn">Add Expense</div>
    </div>
  )
}

export function BalancesScreen() {
  const rows = [['Aditi → Rohan', '$89', 1], ['Meera → You', '$64', 2], ['Kabir → You', '$52', 3]]
  return (
    <div className="s">
      <div className="s-row s-between"><Back /><span className="s-h4">Balances</span><Dots /></div>
      <div className="s-tabs"><b>Simplify</b><span>All balances</span></div>
      <div className="s-note">💡 3 payments can settle everything</div>
      <div className="s-list">
        {rows.map(([t, a, v]) => (
          <div className="s-item" key={t}>
            <Avatar v={v} size={28} />
            <div className="s-grow">
              <div className="s-t">{t}</div>
              <div className="s-mute">{a}</div>
            </div>
            <span className="s-pill">Settle</span>
          </div>
        ))}
      </div>
      <div className="s-center s-hand">Once these are done,<br />everyone is settled up! 🎉</div>
    </div>
  )
}

export function ProjectScreen() {
  const comps = [['📷', 'Camera', 'Sam · 2 days ago', '$100'], ['🔋', 'Battery', 'Leo · 5 days ago', '$60'], ['🎛', 'Flight Controller', 'Priya · 1 week ago', '$120'], ['⚙', '4 Motors', 'Sam · 14 Sep', '$160']]
  return (
    <div className="s">
      <div className="s-row s-between"><div><div className="s-h3">Build a Drone 🚁</div><div className="s-mute">4 members</div></div><Dots /></div>
      <div className="s-tabs s-tabs--3"><b>Overview</b><span>Components</span><span>Balances</span></div>
      <div className="s-card">
        <div className="s-row s-between"><span className="s-mute">Total spent</span><span className="s-mute">of $600</span></div>
        <strong className="s-big">$520</strong>
        <div className="s-bar"><i style={{ width: '87%' }} /></div>
        <div className="s-mute s-right">87%</div>
      </div>
      <div className="s-grid2">
        <div className="s-stat s-stat--ink"><span>You are owed</span><strong>$210</strong></div>
        <div className="s-stat s-stat--hatch"><span>You owe</span><strong>$0</strong></div>
      </div>
      <span className="s-h4">Recent components</span>
      <div className="s-list">
        {comps.map(([ic, t, sub, a]) => (
          <div className="s-item" key={t}>
            <span className="s-ic">{ic}</span>
            <div className="s-grow"><div className="s-t">{t}</div><div className="s-mute">Paid by {sub}</div></div>
            <b>{a}</b>
          </div>
        ))}
      </div>
    </div>
  )
}

export function PaymentRecordedScreen() {
  return (
    <div className="s s-centered">
      <div className="s-check"><Check /></div>
      <div className="s-h3">Payment Recorded!</div>
      <div className="s-mute s-center">You marked $40 as paid to Rohan.</div>
      <div className="s-card s-row">
        <span className="s-ic">🍴</span>
        <div className="s-grow"><div className="s-t">Dinner at Shack</div><strong>$40</strong><div className="s-mute">24 Sep 2026</div></div>
      </div>
      <div className="s-btn">Done</div>
      <div className="s-btn s-btn--ghost">View updated balances</div>
    </div>
  )
}

export function RemindersScreen() {
  const rows = [['Rohan owes you', '$80', 'Dinner at Toscano · due Sun, 28 Sep', 2], ['Meera owes you', '$52', 'Metro Recharge · due Wed, 1 Oct', 3], ['Aditi owes you', '$64', 'Goa Stay · due Fri, 3 Oct', 1]]
  return (
    <div className="s">
      <div className="s-row s-between"><Back /><span className="s-h4">Reminders</span><span /></div>
      <div className="s-tabs"><b>Pending (3)</b><span>Sent</span></div>
      <div className="s-list s-list--cards">
        {rows.map(([t, a, sub, v]) => (
          <div className="s-card" key={t}>
            <div className="s-row">
              <Avatar v={v} size={28} />
              <div className="s-grow"><div className="s-mute">{t}</div><strong>{a}</strong><div className="s-mute">{sub}</div></div>
            </div>
            <span className="s-pill s-pill--wide">Remind</span>
          </div>
        ))}
      </div>
      <div className="s-hand s-center">A gentle nudge keeps friendships strong! 🖤</div>
    </div>
  )
}
