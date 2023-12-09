package com.example.restaurantmenuwithar

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode

@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(route = Screen.MainScreen.route){
            MainScreen(navController = navController)
        }
        composable(route = Screen.DetailScreen.route + "/{name}",
            arguments = listOf(
                navArgument("name"){
                    type = NavType.StringType
                    defaultValue = "burger"
                    nullable = true
                }
            )){entry ->
            entry.arguments?.getString("name")?.let { DetailScreen(name = it) }
        }
    }
}


@Composable
fun MainScreen(navController: NavController){

    FoodMenu(navController)
}


@Composable
fun DetailScreen(name: String){
    ARScreen(model = name)
}



@Composable
fun FoodMenu(navController: NavController) {
    val foodItems = listOf(
        FoodItem("Pizza", "pizza", R.drawable.pizza,"Delicious Italian pizza with your choice of toppings.", 350f),
        FoodItem("Burger", "burger", R.drawable.burger, "Classic juicy burger with cheese, lettuce, and tomato.", 220f),
        FoodItem("Instant Noodles", "instant",R.drawable.instant, "Noodle Nirvana in a Flash!", 150f),
        FoodItem("Momos", "momos",R.drawable.momos, "Bite-sized Bliss, Steamed to Perfection.", 180f),
        FoodItem("Ramen", "ramen",R.drawable.ramen, "Savor the Slurp: Ramen Magic Unleashed!", 230f),
        FoodItem("Nutella Milkshake", "nutella_milkshake",R.drawable.nutella_milkshake, "Chocoholic's Dream: Nutella Bliss in a Glass.",150f),
        FoodItem("Double Hot Chocolate", "double_hot_chocolate",R.drawable.double_hot_chocolate, "Double the Delight: Hot Chocolate Heaven Doubled.", 180f)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Food Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(foodItems) { foodItem ->
                FoodItemCard(foodItem = foodItem, navController)
            }
        }
    }
}




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodItemCard(foodItem: FoodItem, navController: NavController) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = 4.dp,
        onClick = { navController.navigate(Screen.DetailScreen.withArgs(foodItem.name)) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = foodItem.imageId),
                contentDescription = foodItem.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = foodItem.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = foodItem.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "₹${foodItem.price}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


@Composable
fun ARScreen(model:String) {
    val nodes = remember {
        mutableListOf<ArNode>()
    }
    val modelNode = remember {
        mutableStateOf<ArModelNode?>(null)
    }
    val placeModelButton = remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier.fillMaxSize()){
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onCreate = {arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer.isShadowReceiver = false
                modelNode.value = ArModelNode(arSceneView.engine,PlacementMode.BEST_AVAILABLE).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = 0.8f
                    ){

                    }
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = {node, hitResult ->
                        placeModelButton.value = node.isTracking
                    }

                }
                nodes.add(modelNode.value!!)
            },
            onSessionCreate = {
                planeRenderer.isVisible = false
            }
        )
        if(placeModelButton.value){
            Button(onClick = {
                modelNode.value?.anchor()
            }, modifier = Modifier.align(Alignment.Center)) {
                Text(text = "Place Here")
            }
        }
    }



    LaunchedEffect(key1 = model){
        modelNode.value?.loadModelGlbAsync(
            glbFileLocation = "models/${model}.glb",
            scaleToUnits = 0.4f,
            centerOrigin = null,
            autoAnimate = true
        )
        Log.e("errorloading","ERROR LOADING MODEL")
    }
}


data class FoodItem(val displayName: String, val name: String, val imageId: Int,  val description: String, val price: Float)

