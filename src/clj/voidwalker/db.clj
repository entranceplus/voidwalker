(ns voidwalker.db)

(korma.db/defdb dbcon (korma.db/mysql {:user "root"
                                       :password ""
                                       :db "voidwalker"}))
