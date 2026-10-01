// Brand / App Mark and Brand / Logo from the Paybak iOS Figma file (8:22, 8:31): the white "P"
// monogram on a black squircle, next to "Paybak" in Manrope ExtraBold. Same geometry as the apps.
const SQUIRCLE = 'M102.7328 0c20.0454 0 30.0681 0 37.7244 3.9011a35.792 35.792 0 0 1 15.6417 15.6417c3.9011 7.6563 3.9011 17.679 3.9011 37.7244L160 102.7328c0 20.0454 0 30.0681-3.9011 37.7244a35.792 35.792 0 0 1-15.6417 15.6417c-7.6563 3.9011-17.679 3.9011-37.7244 3.9011L57.2672 160c-20.0454 0-30.0681 0-37.7244-3.9011a35.792 35.792 0 0 1-15.6417-15.6417c-3.9011-7.6563-3.9011-17.679-3.9011-37.7244L0 57.2672c0-20.0454 0-30.0681 3.9011-37.7244a35.792 35.792 0 0 1 15.6417-15.6417c7.6563-3.9011 17.679-3.9011 37.7244-3.9011Z'
const GLYPH = 'M55 122.5V37.5H81.25a27.5 27.5 0 0 1 0 55H55'

export function AppMark() {
  return (
    <svg className="app-mark" viewBox="0 0 160 160" aria-hidden="true">
      <path d={SQUIRCLE} fill="#0a0a0a" />
      <path d={GLYPH} fill="none" stroke="#fff" strokeWidth="17.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

export default function Logo({ big = false }) {
  return (
    <span className={`logo ${big ? 'logo--big' : ''}`}>
      <AppMark />
      <span className="logo-word">Paybak</span>
    </span>
  )
}
