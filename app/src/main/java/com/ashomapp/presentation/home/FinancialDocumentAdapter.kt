package com.ashomapp.presentation.home

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.databinding.ItemFinancialStatementsBinding
import com.ashomapp.network.response.dashboard.CompanyStatementsDTO
import com.ashomapp.network.response.dashboard.CountriesDTO

class FinancialDocumentAdapter(val list : List<CustomFinancialDTO>,
                               val mList : List<CompanyStatementsDTO> ?= null,
                               val onDocumentClick: onDocumentClick
                               ): RecyclerView.Adapter<FinancialDocumentAdapter.ViewHolder>() {

    class ViewHolder(val binding : ItemFinancialStatementsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindview(
            financialDTO: CustomFinancialDTO,
            mList: List<CompanyStatementsDTO>?= null,
            onDocumentClick: onDocumentClick
        ) {


               if (!mList.isNullOrEmpty()){
                //   mList!!.map { it.document_name }.contains("Comprehensive Statement")
                   if (mList!!.map { it.document_name }.contains(financialDTO.document_name)
                      ){
                       binding.notAvailableView.visibility  = View.GONE
                       binding.notAvailableTv.visibility = View.GONE
                   }else{
                       if (mList!!.map { it.document_name }.contains("Comprehensive Statement") && financialDTO.document_name.equals("Comprehensive Income Statement")){
                           binding.notAvailableView.visibility  = View.GONE
                           binding.notAvailableTv.visibility = View.GONE
                       }else{
                           binding.notAvailableView.visibility  = View.VISIBLE
                           binding.notAvailableTv.visibility = View.VISIBLE
                       }


                   }

               }else{

                   binding.notAvailableView.visibility  = View.VISIBLE
                   binding.notAvailableTv.visibility = View.VISIBLE
               }
            binding.itemFinancialRoot.setOnClickListener {
                binding.itemFinancialRoot.getBackground().setColorFilter(Color.parseColor("#DEF2FD"), PorterDuff.Mode.SRC_ATOP);
                Handler().postDelayed({
                    binding.itemFinancialRoot.getBackground().setColorFilter(null);

                    if (!binding.notAvailableView.isVisible){
                        ///Income Statement
                       if (financialDTO.document_name.equals("Comprehensive Income Statement")){
                           val mposition = mList!!.map { it.document_name }.indexOf("Comprehensive Statement")
                           onDocumentClick.onDocumentClick(mList[mposition])
                       }else{
                           val mposition = mList!!.map { it.document_name }.indexOf(financialDTO.document_name)
                           onDocumentClick.onDocumentClick(mList[mposition])
                       }
                     }
                }, 100)


            }
            binding.infoIv.setOnClickListener {
              //  Log.d("financialinfo", "${financialDTO.document_name} , ${financialDTO.document_description}")
                onDocumentClick.onInfoClick(
                    CompanyStatementsDTO(
                        financialDTO.document_name,
                        "",
                        financialDTO.document_description
                    )
                )
                if (!binding.notAvailableView.isVisible){
               //     val mposition = mList.map { it.document_name }.indexOf(financialDTO.document_name)

                }
            }
            binding.statementIv.setImageResource(financialDTO.documenet_iamge)
            binding.statementTv.text = financialDTO.document_name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding  = ItemFinancialStatementsBinding.inflate(LayoutInflater.from(parent.context), parent , false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindview(list[position], mList, onDocumentClick)
        if (FinancialStatementfrag.lockunlock == 1){
            holder.binding.unlockIv.setImageResource(R.drawable.un_lock)
            holder.binding.unlockIv.setColorFilter(AshomAppApplication.instance.applicationContext.resources.getColor(R.color.lockcolor));

        }else {
            holder.binding.unlockIv.setImageResource(R.drawable.lock)
            holder.binding.unlockIv.setColorFilter(AshomAppApplication.instance.applicationContext.resources.getColor(R.color.lockcolor));
        }
    }

    override fun getItemCount(): Int {
        return  list.size
    }
}

interface onDocumentClick{
    fun onDocumentClick(companyStatementsDTO: CompanyStatementsDTO )

    fun onInfoClick(companyStatementsDTO: CompanyStatementsDTO)
}