(ns voidwalker.db)

(korma.db/defdb dbcon (korma.db/mysql {:user "void"
                                       :password "walker"
                                       :db "voidwalker"}))
