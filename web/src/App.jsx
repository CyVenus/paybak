import { useCallback, useEffect, useState } from 'react'
import useReveal from './hooks/useReveal'
import Intro from './components/Intro'
import './components/Intro.css'
import Nav from './components/Nav'
import Hero from './components/Hero'
import Marquee from './components/Marquee'
import HowItWorks from './components/HowItWorks'
import SplitDemo from './components/SplitDemo'
import Features from './components/Features'
import ScreensShowcase from './components/ScreensShowcase'
import Stories from './components/Stories'
import NotABank from './components/NotABank'
import Download from './components/Download'
import Footer from './components/Footer'

const reducedMotion = () =>
  typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches

export default function App() {
  useReveal()
  const [intro, setIntro] = useState(() => !reducedMotion())
  const endIntro = useCallback(() => setIntro(false), [])

  useEffect(() => {
    document.documentElement.classList.toggle('is-intro', intro)
  }, [intro])

  return (
    <>
      {intro && <Intro onDone={endIntro} />}
      <div className="grain" aria-hidden="true" />
      <div className="progress" aria-hidden="true" />
      <Nav />
      <main>
        <Hero play={!intro} />
        <Marquee />
        <HowItWorks />
        <SplitDemo />
        <Features />
        <ScreensShowcase />
        <Stories />
        <NotABank />
        <Download />
      </main>
      <Footer />
    </>
  )
}
