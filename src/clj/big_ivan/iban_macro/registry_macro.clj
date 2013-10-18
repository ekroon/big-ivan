(ns big-ivan.iban-macro.registry-macro
  "Short package description."
  (:require [big-ivan.iban-macro.registry :refer [bban-formats->re-string bban-format-map]]))

(defmacro iban-pattern
  "This macro expands to a java Pattern which will match any correctly
structured known IBAN. The registry determines which IBANs are be
supported."
  []
  (re-pattern (bban-formats->re-string bban-format-map)))
