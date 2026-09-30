package app.paybak.paybak.debug

import app.paybak.paybak.data.AvatarChoice
import app.paybak.paybak.data.SignInMethod
import app.paybak.paybak.data.UserProfile

/** The debug seed: the person in the Figma frames. */
internal val SampleProfile =
    UserProfile(
        name = "Arjun Mehta",
        avatar = AvatarChoice.Preset(0),
        currencyCode = "INR",
        upiId = "arjun@okaxis",
        signInMethod = SignInMethod.Email,
        contact = "arjun@example.com",
    )
