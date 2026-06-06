package epn.tallermoviles

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform