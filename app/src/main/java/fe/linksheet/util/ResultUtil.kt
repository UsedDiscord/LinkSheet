package fe.linksheet.util

sealed class Results<out T>(val value: Any?) {
    val isSuccess: Boolean get() = this is Success<T>
    val isError: Boolean get() = this is Error<T>
    val isFailure: Boolean get() = this is Failure
    val isLoading: Boolean get() = this is Loading


    data class Success<out T>(val data: T) : Results<T>(data)
    data class Error<out T>(val data: T): Results<T>(data)

    class Failure(exception: Exception) : Results<Exception>(exception)
    class Loading : Results<Nothing>(Unit)

    companion object {
        fun <T> success(value: T): Success<T> = Success(value)
        fun <T> error(value: T): Error<T> = Error(value)
        fun loading() = Loading()
        fun failure(exception: Exception) = Failure(exception)

        fun boolean(boolean: Boolean): Results<Boolean> {
            return if(boolean) success(true) else error(false)
        }

//        inline fun <T> failure(exception: Throwable): Result<T> =
//            Result(createFailure(exception))
    }
}