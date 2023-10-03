package put.poznan.freshguard.db.users

data class UserSession(
    var username: String = "",
    var isLoggedIn: Boolean = false
)
