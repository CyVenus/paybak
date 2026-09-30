import { useEffect, useState } from 'react'
import { WORDS, TOP, HEIGHT } from './handwriting'

// Pen timing (ms). Each letter's outline is traced, then filled with ink.
const STAGGER = 150
const STROKE = 560
const DOT = 260
const HOLD = 1700
const OUT = 650

const VB_H = HEIGHT + 8
const MAX_W = Math.max(...WORDS.map((w) => w.w))

// Same squiggle as <Squiggle />, stretched to the word's width.
const squiggle = (x, w) => {
  const pts = [[2, 14], [30, 4], [50, 18], [80, 10], [130, 4], [160, 12], [190, 8], [198, 9]]
  const [a, b, c, d, e, f, g, h] = pts.map(([px, py]) => `${(x + (px / 200) * w).toFixed(1)} ${(107 + py * 1.1).toFixed(1)}`)
  return `M${a} C ${b}, ${c}, ${d} S ${e}, ${f} S ${g}, ${h}`
}

const timing = (word) => {
  let t = 0
  const letters = [...word].map((ch) => {
    const dur = ch === '.' ? DOT : STROKE
    const at = t
    t += STAGGER
    return { at, dur }
  })
  const writeEnd = letters.at(-1).at + letters.at(-1).dur
  return { letters, ulAt: writeEnd - 180, total: writeEnd + 520 + HOLD }
}

const reducedMotion = () =>
  typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches

export default function HandWord({ play = true, start = 450 }) {
  const [i, setI] = useState(0)
  const [out, setOut] = useState(false)
  const [first, setFirst] = useState(true)

  const word = WORDS[i]
  const { letters, ulAt, total } = timing(word.word)
  const lead = first ? start : 0

  useEffect(() => {
    if (!play || reducedMotion()) return
    const t1 = setTimeout(() => setOut(true), lead + total)
    const t2 = setTimeout(() => {
      setOut(false)
      setFirst(false)
      setI((n) => (n + 1) % WORDS.length)
    }, lead + total + OUT)
    return () => { clearTimeout(t1); clearTimeout(t2) }
  }, [play, i, lead, total])

  return (
    <span className="hw" style={{ '--w': `${MAX_W / 100}em`, '--h': `${VB_H / 100}em` }}>
      <svg
        key={i}
        className={`hw-word ${out ? 'is-out' : ''} ${play ? '' : 'is-paused'}`}
        viewBox={`${word.x} ${TOP} ${word.w} ${VB_H}`}
        style={{ width: `${word.w / 100}em` }}
        aria-hidden="true"
      >
        {word.letters.map((d, k) => (
          <path
            key={k}
            className="hw-l"
            d={d}
            pathLength="1"
            style={{ '--at': `${lead + letters[k].at}ms`, '--dur': `${letters[k].dur}ms` }}
          />
        ))}
        <path
          className="hw-ul"
          d={squiggle(word.x + 2, word.w - 4)}
          pathLength="1"
          style={{ '--at': `${lead + ulAt}ms` }}
        />
      </svg>
    </span>
  )
}
