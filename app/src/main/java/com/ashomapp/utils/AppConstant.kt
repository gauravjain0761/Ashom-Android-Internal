package com.ashomapp.utils

import androidx.lifecycle.MutableLiveData
import com.ashomapp.network.response.Countrycodelist
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.google.gson.annotations.SerializedName
//https://testnet.ashom.app/api/webservice/recordevent
const val BASE_URL = "https://ashom.app"
const val API_URL = "$BASE_URL/api/"
const val STOCK_API_URL = "http://13.127.45.140/"
const val NOT_FOUND = "An Error Occurred(404)"
const val API_KEY = "ashom@123"
const val WEB_CLIENT_ID = "902457448135-id3akvu4lh553h1mb6in0l8ko8fo9arg.apps.googleusercontent.com"
const val DOB_SCOPE = "https://www.googleapis.com/auth/user.birthday.read"
const val GENDER_SCOPE = "https://www.googleapis.com/auth/user.gender.read"
const val GOOGLE_AUTH_RC = 556

const val GOOGLE_WEB_SERVER_CLIENT_ID = "781793150436-dknhpd8ettnm41haenc36inc7fv6prr2.apps.googleusercontent.com"


const val PLAYSTORE_LICENCE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAly/lHXr35V4VOZnmXbHyXfOjjUvo/ciapqCq4nuZSrkRW/0imhYcuGssu4euQzAaxvxEo4fWaLH+ulYWw6E/08Lsdktq87MD7aLUaSHKRo/TSO/VEr3+E4iYBDxXhIz7YSFbs48PCOCsTMFS5gzJ6bG/P0uc/kS9OK6nM5kreUtvuTcyCKwEM9z+re9sd91UcxMRa23Hu2SBsagigst3wDymaphnu0MTNVoMuNP9V0D+tK1jnGMg6iGkXzf4MHC3TS+Z6DcFbygr2DRFiNz1ihc0lsIFGCRBqF3G8Hz6UaLafUzP08M3ps6iaAfF18tOqX7IiaqgM6ULf169thaLyQIDAQAB"
const val ANNUAL_SUBSCRIPTION_PRO_ID = "com.ashom.yearly.subscriptions\n"

const val PRIVACY_POLICY = 0
const val ABOUT_US = 1
const val TERMS_CONDITION = 2

const val  PRIVACY_POLICY_URL = "https://ashom.app/privacypolicy@Privacy and Policy"
const val ABOUT_US_URL = "https://ashom.app/aboutus@About Us"
const val TERMS_CONDITION_URL = "https://ashom.app/termscondition@Terms and conditions"

const val LINKEDN_APIKEY = "78ofvholrg325a"
const val LINKEDN_SECRETKEY = "KF0GD7kGpwFkR85D"
const val LINKEDN_REDIRECTURI = "https://testnet.ashom.app"
val SCOPE = "r_liteprofile%20r_emailaddress"

val AUTHURL = "https://www.linkedin.com/oauth/v2/authorization"
val TOKENURL = "https://www.linkedin.com/oauth/v2/accessToken"



const val DATABASE_NAME = "ASHOM_DATABASE"
const val DATABASE_VERSION = 3



const val TABLE_NAME = "COUNTRY_LIST_TABLE"
const val ID = "d_id"
const val COMPANY_ID = "id"
const val COMPANY_NAME = "Company_Name"
const val SYMBOLTICKER = "SymbolTicker"
const val COUNTRY = "Country"
const val INDUSTRY = "industry"
const val COMPANY_IMAGE = "image"
const val DELISTINGDATE = "DelistingDate"
const val COMPANY_EXCHANGE = "exchanges"
const val COMPANY_STATUS = "company_status"


val notificationcounter = MutableLiveData<Int>(0)
val selectedCompaniesList = MutableLiveData<List<CompanyDTO>>()
val countrycodelist = listOf<Countrycodelist>(
    Countrycodelist("Afghanistan", "93"),
    Countrycodelist("Albania", "355"),
    Countrycodelist("Algeria", "213"),
    Countrycodelist("American Samoa", "1684"),
    Countrycodelist("Andorra", "376"),
    Countrycodelist("Angola", "244"),
    Countrycodelist("Anguilla", "1264"),
    Countrycodelist("Antarctica", "672"),
    Countrycodelist("Antigua and Barbuda", "1268"),
    Countrycodelist("Argentina", "54"),
    Countrycodelist("Armenia", "374"),
    Countrycodelist("Aruba", "297"),
    Countrycodelist("Australia", "61"),
    Countrycodelist("Austria", "43"),
    Countrycodelist("Azerbaijan", "994"),
    Countrycodelist("Bahamas", "1242"),
    Countrycodelist("Bahrain", "973"),
    Countrycodelist("Bangladesh", "880"),
    Countrycodelist("Barbados", "1246"),
    Countrycodelist("Belarus", "375"),
    Countrycodelist("Belgium", "32"),
    Countrycodelist("Belize", "501"),
    Countrycodelist("Benin", "229"),
    Countrycodelist("Bermuda", "1441"),
    Countrycodelist("Bhutan", "975"),
    Countrycodelist("Bolivia", "591"),
    Countrycodelist("Bosnia and Herzegovina", "387"),
    Countrycodelist("Botswana", "267"),
    Countrycodelist("Brazil", "55"),
    Countrycodelist("Bouvet Island", "47"), ////////////////
    Countrycodelist("BQ", "599"),  //////
    Countrycodelist("British Indian Ocean Territory", "246"),
    Countrycodelist("British Virgin Islands", "1"),    /////
    Countrycodelist("Brunei Darussalam", "673"),
    Countrycodelist("Bulgaria", "359"),
    Countrycodelist("Burkina Faso", "226"),
    Countrycodelist("Burundi", "257"),
    Countrycodelist("Cambodia", "855"),
    Countrycodelist("Cameroon", "237"),
    Countrycodelist("Canada", "1"),
    Countrycodelist("Cape Verde", "238"),
    Countrycodelist("Cayman Islands", "345"),
    Countrycodelist("Central African Republic", "236"),
    Countrycodelist("Chad", "235"),
    Countrycodelist("Chile", "56"),
    Countrycodelist("China", "86"),
    Countrycodelist("Christmas Island", "61"),
    Countrycodelist("Cocos (Keeling) Islands", "61"),
    Countrycodelist("Colombia", "57"),
    Countrycodelist("Comoros", "269"),
    Countrycodelist("Congo", "242"),
    Countrycodelist("Congo, Democratic Republic of the", "243"),
    Countrycodelist("Cook Islands", "682"),
    Countrycodelist("Costa Rica", "506"),
    Countrycodelist("Côte d'Ivoire", "225"),
    Countrycodelist("Croatia", "385"),
    Countrycodelist("Cuba", "53"),
    Countrycodelist("Curacao", "599"),  /////
    Countrycodelist("Cyprus", "537"),
    Countrycodelist("Czech Republic", "420"),
    Countrycodelist("Denmark", "45"),
    Countrycodelist("Djibouti", "253"),
    Countrycodelist("Dominica", "1767"),
    Countrycodelist("Dominican Republic", "1849"),
    Countrycodelist("Ecuador", "593"),
    Countrycodelist("Egypt", "20"),
    Countrycodelist("El Salvador", "503"),
    Countrycodelist("Equatorial Guinea", "240"),
    Countrycodelist("Eritrea", "291"),
    Countrycodelist("Estonia", "372"),
    Countrycodelist("Ethiopia", "251"),
    Countrycodelist("Falkland Islands (Malvinas)", "500"),
    Countrycodelist("Faroe Islands", "298"),
    Countrycodelist("Fiji", "679"),
    Countrycodelist("Finland", "358"),
    Countrycodelist("France", "33"),
    Countrycodelist("French Guiana", "594"),
    Countrycodelist("French Polynesia", "689"),
    Countrycodelist("Gabon", "241"),
    Countrycodelist("Gambia", "220"),
    Countrycodelist("Georgia", "995"),
    Countrycodelist("Germany", "49"),
    Countrycodelist("Ghana", "233"),
    Countrycodelist("Gibraltar", "350"),
    Countrycodelist("Greece", "30"),
    Countrycodelist("Greenland", "299"),
    Countrycodelist("Grenada", "1"),
    Countrycodelist("Guadeloupe", "590"),
    Countrycodelist("Guam", "1671"),
    Countrycodelist("Guatemala", "502"),
    Countrycodelist("Guernsey", "44"),
    Countrycodelist("Guinea", "224"),
    Countrycodelist("Guinea-Bissau", "245"),
    Countrycodelist("Guyana", "595"),
    Countrycodelist("Haiti", "509"),
    Countrycodelist("Holy See (Vatican City State)", "379"),
    Countrycodelist("Honduras", "504"),
    Countrycodelist("Hong Kong", "852"),
    Countrycodelist("Hungary", "36"),
    Countrycodelist("Iceland", "354"),
    Countrycodelist("India", "91"),
    Countrycodelist("Indonesia", "62"),
    Countrycodelist("Iran", "98"),
    Countrycodelist("Iraq", "964"),
    Countrycodelist("Ireland", "353"),
    Countrycodelist("Isle of Man", "44"),
    Countrycodelist("Israel", "972"),
    Countrycodelist("Italy", "39"),
    Countrycodelist("Jamaica", "1876"),
    Countrycodelist("Japan", "81"),
    Countrycodelist("Jersey", "44"),
    Countrycodelist("Jordan", "962"),
    Countrycodelist("Kazakhstan", "77"),
    Countrycodelist("Kenya", "254"),
    Countrycodelist("Kiribati", "686"),
    Countrycodelist("North Korea", "850"),
    Countrycodelist("South Korea", "82"),
    Countrycodelist("Kuwait", "965"),
    Countrycodelist("Kyrgyzstan", "996"),
    Countrycodelist("Laos", "856"),
    Countrycodelist("Latvia", "371"),
    Countrycodelist("Lebanon", "961"),
    Countrycodelist("Lesotho", "266"),
    Countrycodelist("Liberia", "231"),
    Countrycodelist("Libya", "218"),  //libyan arab jamahiriya
    Countrycodelist("Liechtenstein", "423"),
    Countrycodelist("Lithuania", "370"),
    Countrycodelist("Luxembourg", "352"),
    Countrycodelist("Macao", "853"),
    Countrycodelist("Macedonia", "389"),
    Countrycodelist("Madagascar", "261"),
    Countrycodelist("Malawi", "265"),
    Countrycodelist("Malaysia", "60"),
    Countrycodelist("Maldives", "960"),
    Countrycodelist("Mali", "223"),
    Countrycodelist("Malta", "356"),
    Countrycodelist("Marshall Islands", "692"),
    Countrycodelist("Martinique", "596"),
    Countrycodelist("Mauritania", "222"),
    Countrycodelist("Mauritius", "230"),
    Countrycodelist("Mayotte", "262"),
    Countrycodelist("Mexico", "52"),
    Countrycodelist("Micronesia", "691"),
    Countrycodelist("Moldova", "373"),
    Countrycodelist("Monaco", "377"),
    Countrycodelist("Mongolia", "976"),
    Countrycodelist("Montenegro", "382"),
    Countrycodelist("Montserrat", "1664"),
    Countrycodelist("Morocco", "212"),
    Countrycodelist("Mozambique", "258"),
    Countrycodelist("Myanmar", "95"),
    Countrycodelist("Namibia", "264"),
    Countrycodelist("Nauru", "674"),
    Countrycodelist("Nepal", "977"),
    Countrycodelist("Netherlands", "31"),
    Countrycodelist("Netherlands Antilles", "599"),
    Countrycodelist("New Caledonia", "687"),
    Countrycodelist("New Zealand", "64"),
    Countrycodelist("Nicaragua", "505"),
    Countrycodelist("Niger", "227"),
    Countrycodelist("Nigeria", "234"),
    Countrycodelist("Niue", "683"),
    Countrycodelist("Norfolk Island", "672"),
    Countrycodelist("Northern Mariana Islands", "1670"),
    Countrycodelist("Norway", "47"),
    Countrycodelist("Oman", "968"),
    Countrycodelist("Pakistan", "92"),
    Countrycodelist("Palau", "680"),
    Countrycodelist("Palestinian Territory, Occupied", "970"),
    Countrycodelist("Panama", "507"),
    Countrycodelist("Papua New Guinea", "675"),
    Countrycodelist("Paraguay", "595"),
    Countrycodelist("Peru", "51"),
    Countrycodelist("Philippines", "63"),
    Countrycodelist("Pitcairn", "872"),
    Countrycodelist("Poland", "48"),
    Countrycodelist("Portugal", "351"),
    Countrycodelist("Puerto Rico", "1939"),
    Countrycodelist("Qatar", "974"),
    Countrycodelist("Réunion", "262"),
    Countrycodelist("Romania", "40"),
    Countrycodelist("Russia", "7"),
    Countrycodelist("Rwanda", "250"),
    Countrycodelist("Saint Helena", "290"),
    Countrycodelist("Saint Kitts and Nevis", "1869"),
    Countrycodelist("Saint Lucia", "1758"),
    Countrycodelist("Saint Pierre and Miquelon", "508"),
    Countrycodelist("Saint Vincent and the Grenadines", "1784"),
    Countrycodelist("Saint Barthélemy", "590"),
    Countrycodelist("Saint Martin", "590"),
    Countrycodelist("Samoa", "685"),
    Countrycodelist("San Marino", "378"),
    Countrycodelist("Sao Tome and Principe", "239"),
    Countrycodelist("Saudi Arabia", "966"),
    Countrycodelist("Senegal", "221"),
    Countrycodelist("Serbia", "381"),
    Countrycodelist("Seychelles", "248"),
    Countrycodelist("Sierra Leone", "232"),
    Countrycodelist("Singapore", "65"),
    Countrycodelist("Sint Maarten", "1"),
    Countrycodelist("Slovakia", "421"),
    Countrycodelist("Slovenia", "386"),
    Countrycodelist("Solomon Islands", "677"),
    Countrycodelist("Somalia", "252"),
    Countrycodelist("South Africa", "27"),
    Countrycodelist("South Georgia and the South Sandwich Islands", "500"),
    Countrycodelist("South Sudan", "211"),
    Countrycodelist("Spain", "34"),
    Countrycodelist("Sri Lanka", "94"),
    Countrycodelist("Sudan", "249"),
    Countrycodelist("Suriname", "597"),
    Countrycodelist("Svalbard and Jan Mayen", "47"),
    Countrycodelist("Swaziland", "268"),
    Countrycodelist("Sweden", "46"),
    Countrycodelist("Switzerland", "41"),
    Countrycodelist("Syrian Arab Republic", "963"),
    Countrycodelist("Taiwan", "886"),
    Countrycodelist("Tajikistan", "992"),
    Countrycodelist("Tanzania", "255"),
    Countrycodelist("Thailand", "66"),
    Countrycodelist("Timor-Leste", "670"),
    Countrycodelist("Togo", "228"),
    Countrycodelist("Tokelau", "690"),
    Countrycodelist("Tonga", "676"),
    Countrycodelist("Trinidad and Tobago", "1868"),
    Countrycodelist("Tunisia", "216"),
    Countrycodelist("Turkey", "90"),
    Countrycodelist("Turkmenistan", "993"),
    Countrycodelist("Turks and Caicos Islands", "1649"),
    Countrycodelist("Tuvalu", "688"),
    Countrycodelist("Uganda", "256"),
    Countrycodelist("Ukraine", "380"),
    Countrycodelist("United Arab Emirates", "971"),
    Countrycodelist("United Kingdom", "44"),
    Countrycodelist("United States of America", "1"),
    Countrycodelist("Uruguay", "598"),
    Countrycodelist("Uzbekistan", "998"),
    Countrycodelist("Vanuatu", "678"),
    Countrycodelist("Venezuela", "58"),
    Countrycodelist("Viet Nam", "84"),
    Countrycodelist("Virgin Islands, US", "1340"),
    Countrycodelist("Wallis and Futuna", "681"),
    Countrycodelist("Yemen", "967"),
    Countrycodelist("Zambia", "260"),
    Countrycodelist("Zimbabwe", "263")
)

val companyCountrylist = listOf<CountriesDTO>(
    CountriesDTO("KSA"),
    CountriesDTO("Kuwait"),
    CountriesDTO("Qatar"),
    CountriesDTO("Oman"),
    CountriesDTO("UAE"),
    CountriesDTO("Bahrain")
)