var myHeaders = new Headers();
myHeaders.append("Accept", "application/json");
myHeaders.append("X-M2M-RI", "12345");
myHeaders.append("X-M2M-Origin", "SOrigin");

var requestOptions = {
	method: 'GET',
	headers: myHeaders,
	redirect: 'follow'
};


setInterval(function(){
	fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account?fu=1&ty=4", requestOptions)
	.then(response => response.json())
	.then(result => {
		
		var ID_list = []

		var all = 0
		var sudden = 0
		var buff = 0
		var school = 0
		for (var i = 0; i < result["m2m:uril"].length; i++) {

			ID_list[i] = result["m2m:uril"][i].split("/")[4]

			var j = 0

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID_list[i], requestOptions)
			.then(response => response.json())
			.then(result => {
				var rst_list = result["m2m:cin"]["con"].split(" ")

				if (rst_list.length == 13){
					all += Number(rst_list[8])
					sudden += Number(rst_list[9])
					buff += Number(rst_list[10])
					school += Number(rst_list[11])
				}
				j++

				if (j == ID_list.length && all != 0) {
					document.getElementById("sudden").value = sudden / all * 100
					document.getElementById("sudden_ment").innerHTML = Math.round(sudden / all * 100 * 10) / 10 + "% of the total cumulative penalty points" 
					document.getElementById("sudden_bar").style = "height:" + (sudden / all).toFixed(2) * 100 + "%"
					document.getElementById("sudden_bar_").innerHTML = Math.round(sudden / all * 100 * 10) / 10 + "%"

					document.getElementById("buff").value = buff / all * 100
					document.getElementById("buff_ment").innerHTML = Math.round(buff / all * 100 * 10) / 10 + "% of the total cumulative penalty points"
					document.getElementById("buff_bar").style = "height:" + (buff / all).toFixed(2) * 100 + "%"
					document.getElementById("buff_bar_").innerHTML = Math.round(buff / all * 100 * 10) / 10 + "%"

					document.getElementById("school").value = school / all * 100
					document.getElementById("school_ment").innerHTML = Math.round(school / all * 100 * 10) / 10 + "% of the total cumulative penalty points"
					document.getElementById("school_bar").style = "height:" + (school / all).toFixed(2) * 100 + "%"
					document.getElementById("school_bar_").innerHTML = Math.round(school / all * 100 * 10) / 10 + "%"

					document.getElementById("all_bar").style = "height:" + 100 + "%"
					document.getElementById("all_bar_").innerHTML = String(100) + "%"
				}
			})
			.catch(error => console.log('error', error));

			
		}
	})
	.catch(error => console.log('error', error));

}, 1000)

