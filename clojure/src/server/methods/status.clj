(ns server.methods.status
  (:require [clojure.data.json :as json]))

;; somehow auto-injected?

(def serial-version-uid 1)
(def version "0.0.1")
(def git-url "https://github.com/eapearson/eapearsonUiTestModule.git")
(def git-commit-hash "9215d28dccfe128a4814e54715b767bd868ff379")


;; METHODS

(defn call [params _]
  {"state" "OK"
  "message" ""
  "version" version
  "git_url" git-url
  "git_commit_hash" git-commit-hash
})
