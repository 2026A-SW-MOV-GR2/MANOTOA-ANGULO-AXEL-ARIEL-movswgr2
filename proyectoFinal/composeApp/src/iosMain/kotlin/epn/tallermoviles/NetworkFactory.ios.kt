package epn.tallermoviles

actual object NetworkFactory {
    actual fun createPostRepository(): PostRepository = PostRepository(IosPostRemoteDataSource())
}

private class IosPostRemoteDataSource : PostRemoteDataSource {
    override suspend fun consultarPost(id: Int): PostRemoto {
        error("Módulo REST implementado para Android en este proyecto académico.")
    }

    override suspend fun actualizarPost(post: PostRemoto): PostRemoto {
        error("Módulo REST implementado para Android en este proyecto académico.")
    }
}
