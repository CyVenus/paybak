import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import './CatchCash.css'

// Everything runs on one 5000ms CSS clock (see CatchCash.css); the scene then
// holds, fades, and replays.
const CLOCK = 5000
const HOLD = 1900
const FADE = 380
const W = 400

// Hand geometry, in the hand's own frame: wrist at (0,0), fingers up (−y).
// Right hand, palm to camera, so the thumb sits on the viewer's right.
const FINGERS = [
  // [centreX, width, knuckleY, tipY, splay°]
  [29, 19, -80, -142, 5],   // index
  [9, 20, -83, -154, 1],    // middle
  [-11, 19, -80, -146, -3], // ring
  [-30, 17, -72, -118, -8], // pinky
]
const PALM = 'M-27 6 C-31 -12 -40 -36 -41 -62 C-41.5 -72 -38 -80 -30 -82 C-10 -88 18 -89 38 -82 C42 -72 41 -56 37 -44 C45 -30 42 -8 27 6 Z'
const THUMB = 'M-11.5 0 C-12.5 -18 -11.5 -38 -8.5 -49 C-6 -60 6 -60 8.5 -49 C11.5 -38 12.5 -18 11.5 0 C11.5 8 -11.5 8 -11.5 0 Z'
const SPARK = ['M13 29 Q 17 17 23 7', 'M32 53 Q 43 42 55 34', 'M42 77 Q 58 77 73 80']
const DUST = [[-92, -30, 0], [96, -46, 1], [-70, 44, 2], [84, 36, 3], [4, -64, 4]]

const crease = (c, w, y) => `M${c - w * 0.28} ${y} Q ${c} ${y + 2.2} ${c + w * 0.28} ${y}`

function StraightFingers() {
  return FINGERS.map(([c, w, k, tip, a], i) => {
    const len = k - tip
    return (
      <g key={i} transform={`rotate(${a} ${c} ${k})`}>
        <g className="cc-anim cc-fs" style={{ transformOrigin: `${c}px ${k + 12}px`, animationDelay: `${i * 55}ms` }}>
          <rect x={c - w / 2} y={tip} width={w} height={len + 12} rx={w / 2} className="cc-skin" />
          <path d={crease(c, w, tip + len * 0.36)} className="cc-line" />
          <path d={crease(c, w, tip + len * 0.66)} className="cc-line" />
        </g>
      </g>
    )
  })
}

function CurledFingers() {
  return FINGERS.map(([c, w, k, tip], i) => {
    const top = k - 10
    const h = (k - tip) * 0.44 + 10
    const cw = w + 3
    return (
      <g key={i} className="cc-anim cc-fc" style={{ transformOrigin: `${c}px ${top}px`, animationDelay: `${i * 55}ms` }}>
        <rect x={c - cw / 2} y={top} width={cw} height={h} rx={cw / 2} className="cc-skin" />
        <path d={crease(c, cw, top + 7)} className="cc-line" />
        <rect x={c - cw * 0.27} y={top + h - 12} width={cw * 0.54} height={8} rx={3.5} className="cc-nail" />
      </g>
    )
  })
}

function Bill() {
  return (
    <svg viewBox="0 0 150 68" aria-hidden="true">
      <rect x="1.5" y="1.5" width="147" height="65" rx="9" className="cc-bill-body" />
      <rect x="9" y="8.5" width="132" height="51" rx="5" className="cc-bill-inner" />
      <rect x="9" y="8.5" width="13" height="51" rx="5" fill="url(#cc-hatch)" />
      <rect x="128" y="8.5" width="13" height="51" rx="5" fill="url(#cc-hatch)" />
      <ellipse cx="38" cy="34" rx="7.5" ry="6.5" className="cc-bill-mark" />
      <ellipse cx="112" cy="34" rx="7.5" ry="6.5" className="cc-bill-mark" />
      <circle cx="75" cy="34" r="17" className="cc-bill-seal" />
      <text x="75" y="43.5" className="cc-bill-sign">$</text>
    </svg>
  )
}

function Hand({ front }) {
  return (
    <div className="cc-anim cc-hand">
      <svg viewBox="-80 -190 160 200" aria-hidden="true">
        {front ? (
          <>
            <CurledFingers />
            <g transform="translate(35 -40)">
              <g className="cc-anim cc-thumb">
                <path d={THUMB} className="cc-skin" />
                <path d={crease(0, 20, -30)} className="cc-line" />
              </g>
            </g>
          </>
        ) : (
          <>
            <StraightFingers />
            <path d={PALM} className="cc-skin" />
            <path d="M-37 -57 C-18 -64 4 -65 23 -71" className="cc-line" />
            <path d="M33 -44 C15 -48 -6 -46 -25 -39" className="cc-line" />
            <path d="M31 -45 C16 -37 12 -18 18 2" className="cc-line" />
          </>
        )}
      </svg>
    </div>
  )
}

function Arm({ front }) {
  return (
    <div className="cc-anim cc-arm">
      {!front && (
        <svg className="cc-forearm" viewBox="0 0 400 720" aria-hidden="true">
          <path d="M173 322 C170 380 164 430 160 474 L240 474 C236 430 230 380 227 322 Z" fill="var(--paper)" />
          <path d="M173 322 C170 380 164 430 160 474" className="cc-stroke" />
          <path d="M227 322 C230 380 236 430 240 474" className="cc-stroke" />
          <path d="M184 352 Q 192 355 200 353" className="cc-line" />
          <path d="M150 720 L150 480 C150 468 158 461 170 461 L230 461 C242 461 250 468 250 480 L250 720 Z" className="cc-skin" />
          <path d="M151.5 486 H 248.5 V 506 H 151.5 Z" fill="url(#cc-hatch)" />
          <path d="M150 507 H 250" className="cc-stroke" />
          <path d="M176 530 Q 172 560 175 600" className="cc-line" />
          <path d="M226 540 Q 229 568 226 596" className="cc-line" />
        </svg>
      )}
      <Hand front={front} />
    </div>
  )
}

const reducedMotion = () =>
  typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches

export default function CatchCash({ loop = true, caption = 'got paid back.' }) {
  const box = useRef(null)
  const [run, setRun] = useState(0)
  const [leaving, setLeaving] = useState(false)
  const [still] = useState(reducedMotion)

  useLayoutEffect(() => {
    const el = box.current
    const fit = () => el.style.setProperty('--s', el.clientWidth / W)
    fit()
    const ro = new ResizeObserver(fit)
    ro.observe(el)
    return () => ro.disconnect()
  }, [])

  useEffect(() => {
    if (still || !loop) return
    const out = setTimeout(() => setLeaving(true), CLOCK + HOLD)
    const again = setTimeout(() => { setLeaving(false); setRun((r) => r + 1) }, CLOCK + HOLD + FADE)
    return () => { clearTimeout(out); clearTimeout(again) }
  }, [run, loop, still])

  const replay = () => { setLeaving(false); setRun((r) => r + 1) }

  return (
    <div className={`cc ${still ? 'cc--still' : ''}`} ref={box}>
      <div key={run} className={`cc-stage ${leaving ? 'is-leaving' : ''}`} role="img" aria-label="A hand rises and catches a floating dollar bill">
        <svg width="0" height="0" aria-hidden="true" style={{ position: 'absolute' }}>
          <defs>
            <pattern id="cc-hatch" width="5" height="5" patternUnits="userSpaceOnUse" patternTransform="rotate(-45)">
              <path d="M0 0 V5" stroke="var(--ink)" strokeWidth="1.1" opacity="0.5" />
            </pattern>
          </defs>
        </svg>

        <div className="cc-anim cc-caption">{caption}</div>

        <div className="cc-anim cc-halo" />
        {DUST.map(([x, y, i]) => (
          <i key={i} className="cc-anim cc-dust" style={{ '--x': `${x}px`, '--y': `${y}px`, animationDelay: `${i * 170}ms` }} />
        ))}

        <div className="cc-anim cc-rig">
          <Arm />
          <div className="cc-anim cc-bill">
            <div className="cc-anim cc-bill-flutter"><Bill /></div>
          </div>
          <Arm front />
          <svg className="cc-sparks" viewBox="0 0 80 90" aria-hidden="true">
            {SPARK.map((d, i) => <path key={i} d={d} className="cc-anim" style={{ animationDelay: `${i * 70}ms` }} />)}
          </svg>
          <svg className="cc-sparks cc-sparks--l" viewBox="0 0 80 90" aria-hidden="true">
            {SPARK.map((d, i) => <path key={i} d={d} className="cc-anim" style={{ animationDelay: `${40 + i * 70}ms` }} />)}
          </svg>
        </div>
      </div>
      <button className="cc-replay" onClick={replay} aria-label="Replay animation">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4.5 11a7.5 7.5 0 1 1 2.2 5.3M4.5 5v6h6" /></svg>
      </button>
    </div>
  )
}
