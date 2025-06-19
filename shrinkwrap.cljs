#!/usr/bin/env -S npx nbb
(ns shrinkwrap
  {:clj-kondo/config '{:lint-as {promesa.core/let clojure.core/let}}}
  (:require
    [clojure.tools.cli :as cli]
    [nbb.core :refer [*file* invoked-file]]
    [promesa.core :as p]
    ["child_process" :as cp]
    ["path" :as path]
    ["fs" :as fs]
    ["@vercel/ncc$default" :as ncc]))

(defn run-command [cmd]
  (println (str "Running: " cmd))
  (let [result (cp/spawnSync "sh" #js["-c" cmd] #js{:stdio "inherit"})]
    (when (not= (.-status result) 0)
      (println (str "Command failed: " cmd))
      (js/process.exit 1))))

(defn build-binary [input-file output-file]
  (let [mjs-file (str "./.tmp-" (aget (path/parse input-file) "name") ".mjs")]

    (println (str "Building binary from " input-file " to " output-file))

    ;; Step 1: Compile ClojureScript to JavaScript
    (run-command (str "nbb bundle " input-file " -o " mjs-file))

    ;; Step 2: Bundle with ncc programmatically
    (println (str "Bundling " mjs-file " with ncc..."))
    (p/let [ncc-result
            (ncc (fs/realpathSync mjs-file) #js {:minify true :quiet true})
            bundled-code (aget ncc-result "code")]
      (println (str "NCC bundling complete."))

      ;; Step 3: Create executable binary
      (fs/writeFileSync output-file
                         "#!/usr/bin/env -S node --experimental-default-type=module\n")
      (fs/appendFileSync output-file bundled-code)
      (run-command (str "chmod 755 " output-file))

      (println (str "✅ Binary created: " output-file)))))

(def cli-options
  [["-h" "--help"]])

(defn print-usage [summary]
  (println "nbb ClojureScript Binary Builder")
  (println "Usage: ./shrinkwrap.cljs <input-file.cljs> <output-binary-name> [options]")
  (println)
  (println "Options:")
  (println summary))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      errors
      (do
        (doseq [e errors] (println e))
        (js/process.exit 1))

      (:help options)
      (print-usage summary)

      (not= (count arguments) 2)
      (do
        (println "Error: Incorrect number of arguments. Expected <input-file.cljs> <output-binary-name>.")
        (println)
        (print-usage summary)
        (js/process.exit 1))

      :else
      (let [input-file (first arguments)
            output-file (second arguments)]
        (build-binary input-file output-file)))))

(defn get-args [argv]
  (not-empty (js->clj (.slice argv
                              (if
                                (.endsWith
                                  (or (aget argv 1) "")
                                  "node_modules/nbb/cli.js")
                                3 2)))))

(when (= *file* (invoked-file))
  (apply -main (get-args js/process.argv)))
