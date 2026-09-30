import ApkButton from './ApkButton'
import { AndroidIcon, Sparks } from './Doodles'
import { APK_VERSION, APK_SIZE } from '../config'

export default function Download() {
  return (
    <section className="section" id="download">
      <div className="wrap">
        <div className="cta reveal">
          <Sparks className="cta-sparks" />
          <h2>Ready to get<br /><span className="hand hand--xl">paid back?</span></h2>
          <p>Download Paybak for Android and settle your first bill tonight.</p>
          <div className="cta-actions">
            <ApkButton invert>Download APK</ApkButton>
            <span className="soon"> iOS · coming soon</span>
          </div>
          <div className="cta-meta">
            <AndroidIcon />
            <span>{APK_VERSION}</span><i />
            <span>{APK_SIZE}</span><i />
            <span>Android 8.0+</span>
          </div>
          <p className="cta-fine">Tip: allow “Install unknown apps” for your browser when prompted.</p>
        </div>
      </div>
    </section>
  )
}
