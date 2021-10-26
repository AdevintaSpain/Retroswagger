package com.adevinta.retroswagger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.adevinta.spain.retroswagger.annotation.Retroswagger
import kotlinx.android.synthetic.main.activity_main.pet_list
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


private const val SERVICE_URL = "http://petstore.swagger.io/v2/"

@Retroswagger(SERVICE_URL+"swagger.json", "PetStore", 1, true)
class MainActivity : AppCompatActivity(), PetStoreView {
    private val petStorePresenter: PetStorePresenter

    private val petStoreService: PetStoreApiInterface
    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(
                RxJava2CallAdapterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create())
            .baseUrl(SERVICE_URL)
            .client(client)
            .build()

        petStoreService = retrofit.create(PetStoreApiInterface::class.java)
        petStorePresenter = PetStorePresenter(petStoreService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        petStorePresenter.init(this)

        pet_list.layoutManager = LinearLayoutManager(this)
        pet_list.adapter = PetsAdapter(petList(), this)
    }

    override fun onPetFound(pet: Pet) {
        val newList = petList().toMutableList()
        newList.add(pet)
        pet_list.adapter = PetsAdapter(newList, this)
        (pet_list.adapter as PetsAdapter).notifyDataSetChanged()
    }

    override fun onGetPetByIdError() {
        Toast.makeText(this, "Connection error. Failed to retrieve Pets By Status", Toast.LENGTH_SHORT).show()
    }

    private fun petList(): List<Pet> {
        val pets = mutableListOf<Pet>()
        pets.add(Pet(0, Category(0, "dog"), "nuna", listOf(), listOf(), "adopted"))
        pets.add(Pet(0, Category(0, "dog"), "nina", listOf(), listOf(), "adopted"))
        return pets
    }
}
