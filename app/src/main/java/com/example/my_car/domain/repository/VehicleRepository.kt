import com.example.my_car.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>> //
    suspend fun getVehicleById(id: String): Vehicle? //
    suspend fun createVehicle(vehicle: Vehicle) //[cite: 1]
    suspend fun updateVehicle(vehicle: Vehicle) //[cite: 1]
    suspend fun archiveVehicle(id: String) //[cite: 1]
    suspend fun reactivateVehicle(id: String) //[cite: 1]
    suspend fun isPlateAvailable(plate: String, excludingId: String?): Boolean //[cite: 1]
}