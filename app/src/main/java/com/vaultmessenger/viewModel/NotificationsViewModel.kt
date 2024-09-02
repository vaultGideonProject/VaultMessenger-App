import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultmessenger.modules.NotificationRepository
import com.vaultmessenger.viewModel.ErrorsViewModel
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val errorsViewModel: ErrorsViewModel,
    ) : ViewModel() {

    fun sendNotification(
        token: String,
        title: String,
        body: String,
        imageURL: String
        ) {
        viewModelScope.launch {
            try {
                val result = notificationRepository.sendNotification(
                    token =  token,
                    title =  title,
                    body =  body,
                    imageUrl = imageURL
                )
                result.onSuccess { data ->
                    println("Notification sent successfully: $data")
                }.onFailure { error ->
                    errorsViewModel.setError(error.message ?: "An error occurred")
                    println("Error sending notification: ${error.message}")
                }
            }catch (e:Exception){
                errorsViewModel.setError(e.message ?: "An error occurred")
                println("Internal App Error sending notification: ${e.message}")
            }
        }
    }
}
