;;; Copyright 2009 Gary Johnson
;;;
;;; This file is part of CLJ-DISTRICT.
;;;
;;; CLJ-DISTRICT is free software: you can redistribute it
;;; and/or modify it under the terms of the GNU General Public License
;;; as published by the Free Software Foundation, either version 3 of
;;; the License, or (at your option) any later version.
;;;
;;; CLJ-DISTRICT is distributed in the hope that it will be
;;; useful, but WITHOUT ANY WARRANTY; without even the implied warranty
;;; of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;;; General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with CLJ-DISTRICT.  If not, see
;;; <http://www.gnu.org/licenses/>.

(ns district.gssm-interface
  (:refer-clojure)
  (:use district.gssm
	[district.matrix-ops :only (print-matrix)]))

(defn- coord-map-to-matrix
  "Renders a map of {[i j] -> value} into a 2D matrix."
  [coord-map rows cols]
  (let [matrix (make-array Double/TYPE rows cols)]
    (doseq [[i j :as key] (keys coord-map)]
	(aset-double matrix i j (coord-map key)))
    matrix))

(defn view-provisionshed
  "Prints a matrix representation of the location's provisionshed."
  [location rows cols]
  (newline)
  (print-matrix
   (coord-map-to-matrix (find-provisionshed location) rows cols)
   "%7.2f "))

(defn view-benefitshed
  "Prints a matrix representation of the location's benefitshed."
  [location locations rows cols]
  (newline)
  (print-matrix
   (coord-map-to-matrix (find-benefitshed location locations) rows cols)
   "%7.2f "))

(defn view-location-properties
  "Prints a summary of the post-simulation properties of the
  location."
  [location]
  (let [fmt-str (str
		 "%nLocation %s%n"
		 "--------------------%n"
		 "Neighbors: %s%n"
		 "Sunk: %.2f%n"
		 "Used: %.2f%n"
		 "Consumed: %.2f%n"
		 "Carriers Encountered: %d%n"
		 "Source-Val: %.2f%n"
		 "Sink-Prob: %.2f%n"
		 "Use-Prob: %.2f%n"
		 "Consume-Prob: %.2f%n")
	flows (force (:flows location))]
    (printf fmt-str
	    (:id location)
	    (:neighbors location)
	    @(:sunk location)
	    @(:used location)
	    @(:consumed location)
	    (count @(:carrier-bin location))
	    (:source location)
	    (:sink flows)
	    (:use flows)
	    (:consume flows))))

(defn select-menu-action
  "Prompts the user with a menu of choices and returns the number
  corresponding to their selection."
  []
  (printf "%nAction Menu (0 quits):%n")
  (let [prompts ["View Provisionshed" "View Benefitshed"
		 "View Location Properties" "Count Locations"]]
    (dotimes [i (count prompts)]
	(printf " %d) %s%n" (inc i) (prompts i))))
  (print "Choice: ")
  (flush)
  (read))

(defn select-location
  "Prompts for coords and returns the cooresponding location object."
  [locations rows cols]
  (printf "%nInput location coords%n")
  (let [coords [(do (printf "Row [0-%d]: " (dec rows)) (flush) (read))
		(do (printf "Col [0-%d]: " (dec cols)) (flush) (read))]]
    (some #(and (= (:id %) coords) %) locations)))

(defn gssm-interface
  "Takes a benefit and an observation of the relevant features,
   calculates the gssm flows, and provides a simple menu-based
   interface to view the results.  This currently only works on
   observations with grid-based extents."
  [benefit observation trans-threshold]
  (if (not (geospace/grid-extent? observation))
    (println "NOTE: gssm-interface currently only works for observations with grid extents.")
    (let [locations (vals (simulate-service-flows benefit observation trans-threshold))
	  rows (geospace/grid-rows observation)
	  cols (geospace/grid-columns observation)]
      (loop [choice (select-menu-action)]
	(when (not= choice 0)
	  (cond (== choice 1) (view-provisionshed
			       (select-location locations rows cols) rows cols)
		(== choice 2) (view-benefitshed
			       (select-location locations rows cols) locations rows cols)
		(== choice 3) (view-location-properties
			       (select-location locations rows cols))
		(== choice 4) (printf "%n%d%n" (count locations))
		:otherwise    (printf "%nInvalid selection.%n"))
	  (recur (select-menu-action)))))))