;;; -*- mode: clojure ; coding: utf-8 -*-
;;; (c) 2012 Ben Smith-Mannschott -- Distributed under the Eclipse Public License

(ns big-ivan.iban.check
  "implements the ISO-13616 IBAN check digit algorithm"
  #+clj (:refer-clojure :exclude [read read-string])
  #+clj (:use [clojure.tools.reader.edn :only [read read-string]])
  (:require [clojure.string :as string]
            #+cljs [cljs.reader :refer [read-string]]
            #+cljs [goog.string.format :as gformat]))

(defn letter-to-digits [char]
  "Translate the letters A-Z, a-z to integers 10-36 case insensitively.
Input is a string containing a single letter. Result is a sting of two digits."
  (get  {"A" "10", "a" "10",
         "B" "11", "b" "11",
         "C" "12", "c" "12",
         "D" "13", "d" "13",
         "E" "14", "e" "14",
         "F" "15", "f" "15",
         "G" "16", "g" "16",
         "H" "17", "h" "17",
         "I" "18", "i" "18",
         "J" "19", "j" "19",
         "K" "20", "k" "20",
         "L" "21", "l" "21",
         "M" "22", "m" "22",
         "N" "23", "n" "23",
         "O" "24", "o" "24",
         "P" "25", "p" "25",
         "Q" "26", "q" "26",
         "R" "27", "r" "27",
         "S" "28", "s" "28",
         "T" "29", "t" "29",
         "U" "30", "u" "30",
         "V" "31", "v" "31",
         "W" "32", "w" "32",
         "X" "33", "x" "33",
         "Y" "34", "y" "34",
         "Z" "35", "z" "35"} char))

(defn letters->digits
  "Expand each ASCII letter in s into two digits as per letter->digits.
s must be a string containing only letters and digits."
  [s]
  {:pre [(string? s) (re-matches #"[A-Za-z0-9]*" s)]
   :post [(re-matches #"[0-9]*" %) (<= (count s) (count %))]}
  (string/replace s #"[A-Za-z]{1}" letter-to-digits))

(defn- mod-reduce [str divisor]
  (reduce (fn [m n] (mod (+ (* 10 m) (read-string n)) 97)) 0 (seq str)))

(defn check?
  "Are the check digits in s correct according to ISO-13616?
Check digits can only be verified if s is a string of at least length
4 consisting of exclusively of letters and digits. False is returned
if s does not meet these requirements."
  [^String s]
  (and (string? s)
       (re-matches #"[A-Za-z0-9]{4,}" s)
       (-> (str (.substring s 4) (.substring s 0 4))
           letters->digits
           #+cljs (mod-reduce 97)
           #+clj bigint
           #+clj (mod 97)
           (= 1))))

(defn set-check
  "Compute and insert check digits for indexes 2 and 3 of string s
such that the result is consitent for check? s must be a string which
begins with two upper case letters followed by two digits followed by
any number of letters or digits."
  [^String s]
  {:pre [(re-matches #"[A-Z]{2}[0-9]{2}[A-Za-z0-9]*" s)]
   :post [(check? %)]}
  (let [cc (.substring s 0 2)
        bban (.substring s 4)
        ck (- 98 (int (mod-reduce
                       (letters->digits
                        (str bban cc "00"))
                       97)))]
    (str cc #+cljs (goog.string.format "%02d" ck) #+clj (format "%02d" ck) bban)))
