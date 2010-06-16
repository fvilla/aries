;;; Copyright 2010 Gary Johnson
;;;
;;; This file is part of clj-span.
;;;
;;; clj-span is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU General Public License as published
;;; by the Free Software Foundation, either version 3 of the License,
;;; or (at your option) any later version.
;;;
;;; clj-span is distributed in the hope that it will be useful, but
;;; WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with clj-span.  If not, see <http://www.gnu.org/licenses/>.
;;;
;;;-------------------------------------------------------------------
;;;
;;; This namespace is meant to be compiled as a Java class and called
;;; from the command line with its arguments passed as strings.  It
;;; validates all the inputs, and gives helpful usage messages if
;;; there are errors.  If everything checks out, it converts them to a
;;; map of {keywords -> native clojure types} and passes this to the
;;; run-span function, requesting a command line menu for the results.

(ns clj-span.commandline
  (:gen-class)
  (:use [clj-span.core                :only (run-span)]
        [clj-misc.utils               :only (def- defmulti- &)]
        [clj-span.worldgen            :only (read-layer-from-file)]
        [clojure.set :as set          :only (difference)]
        [clojure.contrib.duck-streams :only (file-str)]))

(def- usage-message
  (str
   "Usage: java -cp clj-span-standalone.jar clj_span.commandline \\ \n"
   "            -source-layer       <filepath> \\ \n"
   "            -sink-layer         <filepath> \\ \n"
   "            -use-layer          <filepath> \\ \n"
   "            -flow-layers        <filepath> \\ \n"
   "            -source-threshold   <double>   \\ \n"
   "            -sink-threshold     <double>   \\ \n"
   "            -use-threshold      <double>   \\ \n"
   "            -trans-threshold    <double>   \\ \n"
   "            -rv-max-states      <integer>  \\ \n"
   "            -downscaling-factor <number>   \\ \n"
   "            -source-type        <finite|infinite> \\ \n"
   "            -sink-type          <finite|infinite> \\ \n"
   "            -use-type           <finite|infinite> \\ \n"
   "            -benefit-type       <rival|non-rival> \\ \n"
   "            -flow-model         <line-of-sight|proximity|carbon|sediment>\n"))

(defmulti- print-usage (fn [error-type extra-info] error-type))

(defmethod print-usage :args-not-even [_ _]
           (println (str "\nError: The number of input arguments must be even.\n\n" usage-message)))

(defmethod print-usage :param-errors [_ extra-info]
           (let [error-message (apply str (interpose "\n\t" extra-info))]
             (println (str "\nError: The parameter values that you entered are incorrect.\n\t" error-message "\n\n" usage-message))))

(def- param-tests
  [["-source-layer"       #(.canRead (file-str %))  " is not readable."          ]
   ["-sink-layer"         #(.canRead (file-str %))  " is not readable."          ]
   ["-use-layer"          #(.canRead (file-str %))  " is not readable."          ]
   ["-flow-layers"        #(.canRead (file-str %))  " is not readable."          ]
   ["-source-threshold"   (& float?   read-string)  " is not a double."          ]
   ["-sink-threshold"     (& float?   read-string)  " is not a double."          ]
   ["-use-threshold"      (& float?   read-string)  " is not a double."          ]
   ["-trans-threshold"    (& float?   read-string)  " is not a double."          ]
   ["-rv-max-states"      (& integer? read-string)  " is not an integer."        ]
   ["-downscaling-factor" (& number?  read-string)  " is not a number."          ]               
   ["-source-type"        #{"finite" "infinite"}    " must be one of finite or infinite."]
   ["-sink-type"          #{"finite" "infinite"}    " must be one of finite or infinite."]
   ["-use-type"           #{"finite" "infinite"}    " must be one of finite or infinite."]
   ["-benefit-type"       #{"rival" "non-rival"}    " must be one of rival or non-rival."]
   ["-flow-model"         #{"line-of-sight" "proximity" "carbon" "sediment"}
    " must be one of line-of-sight, proximity, carbon, or sediment."]])

(defn- valid-params?
  [params]
  (let [errors (remove nil?
                       (map (fn [[key test? error-msg]]
                              (if-let [val (params key)]
                                (if-not (test? val) (str val error-msg))
                                (str "No value provided for " key)))
                            param-tests))
        input-keys   (set (keys params))
        valid-keys   (set (map first param-tests))
        invalid-keys (set/difference input-keys valid-keys)
        errors (concat errors (map #(str % " is not a valid parameter name.") invalid-keys))]
    (if (empty? errors)
      true
      (print-usage :param-errors errors))))

(defn- strings-to-better-types
  "Converts input-params from a map of {strings -> strings} into a map
   of {keywords -> native clojure types}."
  [input-params]
  (-> {}
      (assoc :source-layer       (read-layer-from-file (input-params "-source-layer")))
      (assoc :sink-layer         (read-layer-from-file (input-params "-sink-layer")))
      (assoc :use-layer          (read-layer-from-file (input-params "-use-layer")))
      (assoc :flow-layers        (read-layer-from-file (input-params "-flow-layers")))
      (assoc :source-threshold   (read-string (input-params "-source-threshold")))
      (assoc :sink-threshold     (read-string (input-params "-sink-threshold")))
      (assoc :use-threshold      (read-string (input-params "-use-threshold")))
      (assoc :trans-threshold    (read-string (input-params "-trans-threshold")))
      (assoc :rv-max-states      (read-string (input-params "-rv-max-states")))
      (assoc :downscaling-factor (read-string (input-params "-downscaling-factor")))
      (assoc :source-type        (keyword (input-params "-source-type")))
      (assoc :sink-type          (keyword (input-params "-sink-type")))
      (assoc :use-type           (keyword (input-params "-use-type")))
      (assoc :benefit-type       (keyword (input-params "-benefit-type")))
      (assoc :flow-model         ({"line-of-sight" "LineOfSight", "proximity" "Proximity", "carbon" "Carbon", "sediment" "Sediment"}
                                  (input-params "-flow-model")))))

(defn -main
  "The compiled Java class' main method.  Pass it all the SPAN inputs
   as -key value argument pairs from the command line.  After
   validating your inputs and running the flow simulation, it will
   present a text-based menu to view the model results."
  [& args]
  (if (odd? (count args))
    (print-usage :args-not-even nil)
    (let [input-params (into {} (map vec (partition 2 args)))]
      (when (valid-params? input-params)
        (println "\nAll inputs are valid.\n")
        (doseq [[key _ _] param-tests] (println (find input-params key)))
        (newline)
        (run-span (assoc (strings-to-better-types input-params) :result-type :cli-menu))
        (shutdown-agents)
        (flush)
        (System/exit 0)))))