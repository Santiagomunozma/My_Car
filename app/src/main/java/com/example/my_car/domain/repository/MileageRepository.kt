import com.example.my_car.domain.model.MileageRecord
import kotlinx.coroutines.flow.Flow

interface MileageRepository {
    fun observeMileage(vehicleId: String): Flow<List<MileageRecord>> //[cite: 1]
    suspend fun addMileage(reading: MileageRecord) //[cite: 1]
    suspend fun getLatestMileage(vehicleId: String): MileageRecord? //[cite: 1]
}