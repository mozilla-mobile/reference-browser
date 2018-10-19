This branch is a WIP for hooking up the "places" component in the reference
browser. It will only work with
[this android-components branch](https://github.com/mhammond/android-components/tree/autocomplete-places),
and in particular, after following
[these instructions](https://github.com/mhammond/android-components/blob/autocomplete-places/components/service/sync-places/README.md)

There's a new `BrowserAutocompleteProvider.kt` which wraps both the
`DomainAutocompleteProvider` and the places component. It first tries to
find matches using places, then using the `DomainAutocompleteProvider`.

Note that because we don't yet have geckoview hooked up to collect visit
information and write them to the places database. the places database will
be empty - meaning only
suggestions from `DomainAutocompleteProvider` will be shown. However, there's
a trick you can use to import your desktop places database and have it use
that.

* You will need to check out the application-services repo, and from the
  `places` directory (soon to be `components/places`), execute:

    `cargo run --release --example autocomplete -- --import-places auto`

  (alternatively, in the place of `auto`, specify the full path to a desktop
  `places.sqlite`)

  This will also start a demo-app where you can perform auto-complete queries.
  Press ESC to exit the app, and note that in the same directory you will have
  a file `new-places.db` - rename this file to `places.sqlite`

* Kill the app in the emulator.

* Use the "Device File Explorer" in Android Studio, and navigate to the
  `sdcard/Android/data/org.mozilla.reference.browser/files` directory - if
  you've run the app before, you should already find a `places.sqlite` there.
  Upload the file created above here and restart the app.

Note that if you have many many visits (eg, mine has ~170k places with ~230k
visits with a db size of ~75MB) a search for a full domain may take a couple
of seconds, but we know how to fix this. Even with a database this size,
searches of small substrings (eg, a few letters) are fast. We never expect a
"real" mobile device database to be this large, but it's a nice stress-test.
