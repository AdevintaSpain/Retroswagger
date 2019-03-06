package com.schibsted.retroswagger

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class PetStorePresenter(private val petStoreService: PetStoreApiInterface) {
    private var view: PetStoreView = NullView()
    private val compositeDisposable = CompositeDisposable()

    fun init(petStoreView: PetStoreView) {
        view = petStoreView

        val disposable = petStoreService.getPetById(545646671)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                pet ->
                run {
                    view.onPetFound(pet)
                }
            }, {
                run {
                    view.onGetPetByIdError()
                }
            })
        compositeDisposable.add(disposable)
    }

}

class NullView: PetStoreView {
    override fun onGetPetByIdError() {
    }

    override fun onPetFound(pet: Pet) {
    }
}

interface PetStoreView {
    fun onPetFound(pet: Pet)
    fun onGetPetByIdError()
}
