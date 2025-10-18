#!/usr/bin/env bb
(ns start
  (:require [babashka.process :refer [shell]]
            [babashka.fs      :as fs]
            [babashka.pods    :as pods]
            [clojure.edn      :as edn]
            [clojure.string   :as str]))

(defn env [k default]
  (let [v (System/getenv k)]
    (if (str/blank? v) default v)))

(defn ensure-tools-deps-pod []
  (pods/load-pod 'org.babashka/tools-deps-native "0.1.7")
  (require 'clojure.tools.deps))

(defn parse-edn [x] (if (string? x) (edn/read-string x) x))

(defn lein->deps-map [v]
  (into {} (map (fn [[lib ver & _]] [(symbol lib) {:mvn/version ver}]) v)))

(defn ensure-deps-map [deps]
  (let [d (parse-edn deps)]
    (cond
      (map? d)                                   d
      (and (vector? d) (vector? (first d)))      (lein->deps-map d)
      :else (throw (ex-info "Unsupported dependency format" {:got d})))))

(defn download-deps! [deps dir]
  (ensure-tools-deps-pod)
  (fs/create-dirs dir)
  (let [deps-map     (ensure-deps-map deps)
        create-basis (requiring-resolve 'clojure.tools.deps/create-basis)
        basis        (create-basis {:extra {:deps deps-map}})
        jars         (->> (:classpath-roots basis)
                          (filter #(str/ends-with? % ".jar")))]
    (doseq [jar jars
            :let [dest (fs/file dir (fs/file-name jar))]
            :when (not (fs/exists? dest))]
      (fs/copy jar dest))
    jars))

(def cfg-path "/tmp/config.edn")

(if-let [cfg-str (seq (env "DATAHIKE_CONFIG_EDN" ""))]
  (do (println "Writing server config from DATAHIKE_CONFIG_EDN")
      (spit cfg-path cfg-str))
  (let [port   (or (env "DATAHIKE_PORT" nil) (env "PORT" nil) "4444")
        stores (->> (str/split (env "DATAHIKE_STORES" "") #"[ ,]+")
                    (remove empty?)
                    vec
                    pr-str)
        template {:port     (Integer/parseInt port)
                  :level    (keyword (env "DATAHIKE_LEVEL" "info"))
                  :dev-mode (Boolean/parseBoolean (env "DATAHIKE_DEV_MODE" "false"))
                  :token    (env "DATAHIKE_TOKEN" "securerandompassword")
                  :stores   (edn/read-string stores)}]
    (spit cfg-path (pr-str template))))

(let [extra-deps (env "DATAHIKE_EXTRA_DEPS" nil)]
  (when extra-deps
    (let [jars (download-deps! extra-deps "/opt/datahike/jars")]
      (println "Downloaded" (count jars) "dependencies"))))

(let [classpath "/opt/datahike/*:/opt/datahike/jars/*"]
  (println "Config complete. Starting Datahike HTTP server...")
  (shell {:inherit true}
         "java" "-cp" classpath
         "datahike.http.server" cfg-path))