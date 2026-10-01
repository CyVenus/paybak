import { RELEASES_URL } from '../config'
import { DownloadIcon } from './Doodles'

export default function ApkButton({ small = false, invert = false, children = 'Download APK' }) {
  return (
    <a
      className={`btn ${small ? 'btn--sm' : ''} ${invert ? 'btn--invert' : ''}`}
      href={RELEASES_URL}
      target="_blank"
      rel="noopener noreferrer"
    >
      <DownloadIcon />
      <span>{children}</span>
    </a>
  )
}
