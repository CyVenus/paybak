import { useEffect, useState } from 'react'
import ApkButton from './ApkButton'

export default function Nav() {
  const [scrolled, setScrolled] = useState(false)
  const [open, setOpen] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 12)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  const links = [['How it works', '#how'], ['Try a split', '#split'], ['Features', '#features'], ['Screens', '#screens'], ['Stories', '#stories']]

  return (
    <header className={`nav ${scrolled ? 'nav--scrolled' : ''}`}>
      <div className="wrap nav-inner">
        <a href="#top" className="logo" onClick={() => setOpen(false)}>Paybak</a>
        <nav className={`nav-links ${open ? 'is-open' : ''}`}>
          {links.map(([l, h]) => (
            <a key={h} href={h} onClick={() => setOpen(false)}>{l}</a>
          ))}
        </nav>
        <div className="nav-cta">
          <ApkButton small />
          <button className="burger" aria-label="Menu" aria-expanded={open} onClick={() => setOpen(!open)}>
            <span /><span />
          </button>
        </div>
      </div>
    </header>
  )
}
