import Phone from './Phone'
import { Squiggle } from './Doodles'
import {
  AddExpenseScreen,
  BalancesScreen,
  ProjectScreen,
  PaymentRecordedScreen,
  RemindersScreen,
  HomeScreen,
} from './screens'

const SCREENS = [
  ['Add Expense', AddExpenseScreen],
  ['Balances', BalancesScreen],
  ['Projects', ProjectScreen],
  ['Payment recorded', PaymentRecordedScreen],
  ['Reminders', RemindersScreen],
  ['Dashboard', HomeScreen],
]

export default function ScreensShowcase() {
  return (
    <section className="section section--paper2" id="screens">
      <div className="wrap">
        <div className="section-head reveal">
          <span className="eyebrow">A peek inside</span>
          <h2>Designed to feel <span className="hl">light<Squiggle /></span></h2>
          <p className="lead">Clean screens, friendly words, zero clutter.</p>
        </div>
        <div className="shelf" aria-label="App screens">
          {SCREENS.map(([label, Screen], i) => (
            <figure className="shelf-item reveal" key={label} style={{ '--tilt': `${i % 2 ? 2 : -2}deg` }}>
              <Phone label={label}><Screen /></Phone>
              <figcaption>{label}</figcaption>
            </figure>
          ))}
        </div>
      </div>
    </section>
  )
}
