package com.simplu.transcribetab.tablist

import androidx.lifecycle.ViewModel
import com.simplu.transcribetab.database.TablatureDatabaseDao


class TabListViewModel(dataSource: TablatureDatabaseDao): ViewModel() {

    val tabs = dataSource.getAllTabs()




}