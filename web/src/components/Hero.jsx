import ApkButton from './ApkButton'
import Phone from './Phone'
import { HomeScreen } from './screens'
import HandWord from './HandWord'
import { Sparks, Arrow, Avatar } from './Doodles'
import { APK_VERSION } from '../config'

export default function Hero({ play = true }) {
  return (
    <section className="hero" id="top">
      <div className="wrap hero-grid">
        <div className="hero-copy">
          <span className="eyebrow rise">✦ The friendliest money ledger</span>
          <h1 className="rise hero-title" aria-label="Split. Track. Settle.">
            <HandWord play={play} />
          </h1>
          <p className="lead rise">
            No awkward money talks anymore. Paybak remembers who paid, who owes,
            how much, by when, and in which currency — so friends stay friends.
          </p>
          <div className="hero-actions rise">
            <ApkButton />
          </div>
          <div className="hero-proof rise">
            <div className="stack">
              {[0, 1, 2, 3, 4].map((v) => <Avatar key={v} v={v} size={34} />)}
            </div>
            <span>Android {APK_VERSION} · Free · iOS coming soon</span>
          </div>
        </div>

        <div className="hero-art">
          <Sparks className="hero-sparks" />
          <div className="float-card float-card--a">
            <span>You are owed</span>
            <strong>$80</strong>
          </div>
          <div className="float-card float-card--b">
            <span>You owe</span>
            <strong>$20</strong>
          </div>
          <Phone className="hero-phone" label="Paybak home dashboard">
            <HomeScreen />
          </Phone>
          <div className="hero-note">
            <Arrow />
            <span>one glance.<br />who owes you.</span>
          </div>
        </div>
      </div>
    </section>
  )
}
