import { IOS_SOURCE_URL } from '../config'

// iOS has no store build yet, so "iOS" is a plain link to the SwiftUI source on GitHub.
export default function IosLink() {
  return (
    <a
      className="ios-link"
      href={IOS_SOURCE_URL}
      target="_blank"
      rel="noopener noreferrer"
      title="iOS source on GitHub"
    >
      iOS
    </a>
  )
}
