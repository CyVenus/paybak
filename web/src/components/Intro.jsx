import { useEffect, useState } from 'react'

// Timeline (ms). Tiles/lines run on one 3000ms CSS clock; the wordmark follows.
const T = {
  lettersStart: 3500,
  letterStep: 55,
  sparks: 3850,
  tagline: 4000,
  exit: 4900,
  exitDur: 750,
}
const WORD = 'Paybak'

const I = {
  dollar: <path d="M12 3.5v17M16.5 7.5c-.7-1.4-2.4-2.3-4.5-2.3-2.6 0-4.4 1.3-4.4 3.2 0 4.3 9 2.4 9 6.9 0 1.9-2 3.3-4.6 3.3-2.2 0-4-.9-4.7-2.4" />,
  percent: (<><path d="M18 6 6 18" /><circle cx="7.5" cy="7.5" r="2.2" /><circle cx="16.5" cy="16.5" r="2.2" /></>),
  users: (<><circle cx="9" cy="8.5" r="3" /><path d="M3.5 19c0-3 2.5-5 5.5-5s5.5 2 5.5 5" /><path d="M15.5 5.6a3 3 0 0 1 0 5.8M17 14.2c2.3.5 3.6 2.3 3.6 4.8" /></>),
  check: <path d="M5 12.5 9.5 17 19 7" />,
  bell: (<><path d="M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15Z" /><path d="M10 20.5a2 2 0 0 0 4 0" /></>),
  calendar: (<><rect x="4" y="5.5" width="16" height="14.5" rx="3" /><path d="M4 10.5h16M9 3.5v4M15 3.5v4" /></>),
  swap: <path d="M5 9h13l-3.5-3.5M19 15H6l3.5 3.5" />,
  chart: <path d="M6 19v-6M12 19V6M18 19v-9" />,
  repeat: (<><path d="M4.5 11a7.5 7.5 0 0 1 13-4.2L19.5 9" /><path d="M19.5 4.5V9H15" /><path d="M19.5 13a7.5 7.5 0 0 1-13 4.2L4.5 15" /><path d="M4.5 19.5V15H9" /></>),
  chat: <path d="M5 5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-8l-5 3.5V17H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z" />,
  scan: <path d="M4 8.5V5h3.5M16.5 5H20v3.5M20 15.5V19h-3.5M7.5 19H4v-3.5M8 12h8" />,
  divide: (<><path d="M5 12h14" /><circle cx="12" cy="6.5" r="1.3" fill="currentColor" /><circle cx="12" cy="17.5" r="1.3" fill="currentColor" /></>),
  plus: <path d="M12 5v14M5 12h14" />,
  flag: <path d="M6 21V4h11l-2 4 2 4H6" />,
  lock: (<><rect x="5" y="11" width="14" height="9" rx="2.5" /><path d="M8.5 11V8a3.5 3.5 0 0 1 7 0v3" /></>),
  drone: (<><rect x="9" y="10" width="6" height="5" rx="1.5" /><path d="M9 12.5H6.5V7M15 12.5h2.5V7M3.5 7h6M14.5 7h6" /></>),
}

// Inner ring: flies out to a tight 3×3 (p1), then spreads (p2).
const RING = [
  [-1, -1, 'dollar'], [0, -1, 'users', { flash: 1640 }], [1, -1, 'check', { fill: 1900 }],
  [-1, 0, 'percent'], [1, 0, 'bell', { flash: 1760 }],
  [-1, 1, 'plus'], [0, 1, 'divide'], [1, 1, 'swap', { flash: 2060 }],
]
// Outer ring: pops in once the cluster spreads.
const OUTER = [
  [-1, -2, 'pill'], [0, -2, 'calendar'], [1, -2, null, { ghost: true }],
  [-2, -1, null, { ghost: true }], [2, -1, 'chart'],
  [-2, 0, 'scan'], [2, 0, 'chat', { flash: 2180 }],
  [-2, 1, 'repeat', { ghost: true }], [2, 1, 'drone'],
  [-1, 2, null, { ghost: true }], [0, 2, 'flag'], [1, 2, 'lock', { fill: 2240 }],
]
const DUST = [[-150, -40], [140, -90], [-90, 120], [120, 110], [20, -160], [-170, 60]]
const U1 = 44
const U2 = 66

function Glyph({ name }) {
  if (name === 'pill') {
    return (
      <svg viewBox="0 0 60 24" aria-hidden="true">
        <circle cx="13" cy="12" r="3" fill="currentColor" stroke="none" />
        <path d="M23 12h24" />
      </svg>
    )
  }
  if (!name) return null
  return <svg viewBox="0 0 24 24" aria-hidden="true">{I[name]}</svg>
}

function Tile({ name, opts = {}, style, className }) {
  const cls = ['it', className, name === 'pill' && 'it--wide', opts.ghost && 'it--ghost'].filter(Boolean).join(' ')
  const fillStyle = opts.flash != null
    ? { animation: `it-flash 520ms ease ${opts.flash}ms both` }
    : opts.fill != null
      ? { animation: `it-fill 260ms cubic-bezier(.34,1.56,.64,1) ${opts.fill}ms both` }
      : null
  return (
    <span className={cls} style={style}>
      <Glyph name={name} />
      {fillStyle && (
        <span className="it-fill" style={fillStyle}>
          <Glyph name={name} />
        </span>
      )}
    </span>
  )
}

export default function Intro({ onDone }) {
  const [leaving, setLeaving] = useState(false)
  useEffect(() => {
    const t = setTimeout(() => setLeaving(true), T.exit)
    return () => clearTimeout(t)
  }, [])

  useEffect(() => {
    if (!leaving) return
    const t = setTimeout(onDone, T.exitDur)
    return () => clearTimeout(t)
  }, [leaving, onDone])

  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && setLeaving(true)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  return (
    <div className={`intro ${leaving ? 'is-leaving' : ''}`} aria-label="Paybak is loading" role="status">
      <div className="intro-stage">
        {DUST.map(([x, y], i) => (
          <i key={i} className="it-dust" style={{ '--x': `${x}px`, '--y': `${y}px`, animationDelay: `${250 + i * 40}ms` }} />
        ))}

        {/* Brackets sit outside the spread cluster (tiles reach ±149px) so they never cross a tile. */}
        <svg className="it-lines" viewBox="-200 -200 400 400" aria-hidden="true">
          <path d="M-8 -176 H 144 Q 176 -176 176 -144 V -40" />
          <path d="M8 176 H -144 Q -176 176 -176 144 V 40" />
        </svg>

        <span className="it-seed"><Glyph name="dollar" /></span>

        <span className="it-receipt">
          <svg viewBox="0 0 84 84" aria-hidden="true">
            <path d="M24 26h12" />
            <path d="M24 40h36" />
            <path d="M24 50h36" />
            <path d="M24 60h20" />
          </svg>
        </span>

        {RING.map(([dx, dy, name, opts], i) => (
          <Tile
            key={`r${i}`}
            className="it--ring"
            name={name}
            opts={opts}
            style={{
              '--x1': `${dx * U1}px`, '--y1': `${dy * U1}px`,
              '--x2': `${dx * U2}px`, '--y2': `${dy * U2}px`,
              animationDelay: `${i * 28}ms`,
            }}
          />
        ))}

        {OUTER.map(([dx, dy, name, opts], i) => (
          <Tile
            key={`o${i}`}
            className="it--outer"
            name={name}
            opts={opts}
            style={{
              '--x0': `${dx * U2 * 0.55}px`, '--y0': `${dy * U2 * 0.55}px`,
              '--x2': `${dx * U2}px`, '--y2': `${dy * U2}px`,
              animationDelay: `${i * 26}ms`,
            }}
          />
        ))}

        <div className="intro-lockup">
          <div className="intro-word">
            <span className="intro-name" aria-label={WORD}>
              {WORD.split('').map((ch, i) => (
                <span key={i} aria-hidden="true" style={{ animationDelay: `${T.lettersStart + i * T.letterStep}ms` }}>{ch}</span>
              ))}
              <svg className="intro-sparks" viewBox="0 0 80 90" aria-hidden="true">
                <path d="M13 29 Q 17 17 23 7" style={{ animationDelay: `${T.sparks}ms` }} />
                <path d="M32 53 Q 43 42 55 34" style={{ animationDelay: `${T.sparks + 90}ms` }} />
                <path d="M42 77 Q 58 77 73 80" style={{ animationDelay: `${T.sparks + 180}ms` }} />
              </svg>
            </span>
            <span className="intro-tag">
              <span className="intro-tag-1" style={{ animationDelay: `${T.tagline}ms` }}>Split. Track. Settle.</span>
              <span className="intro-tag-2" style={{ animationDelay: `${T.tagline + 120}ms` }}>No awkward money talks anymore.</span>
            </span>
          </div>
        </div>
      </div>

      <button className="intro-skip" onClick={() => setLeaving(true)}>Skip</button>
    </div>
  )
}
