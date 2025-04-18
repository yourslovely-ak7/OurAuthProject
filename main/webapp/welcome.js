
async function getData()
{
	const response= await fetch(`/OurAuth/client`,{
		method: 'GET',
	});
	
	if(response.ok)
	{
		const data= await response.json();
		display(data.clientData);
	}
	else
	{
		alert('Error occurred while getting data!');
	}
}

function display(data)
{
	const container= document.getElementById('dataContainer');
	container.innerHTML='';
	
	console.log(data);
	data.forEach(iter =>{
		const element= document.createElement('div');
		element.classList.add('element');
		
		element.innerHTML=`
							<div class='row'>
								<h5>Name</h5>
								<p>${iter.clientName}</p>
							</div>
							<div class='row'>
								<h5>Client ID</h5>
								<p>${iter.clientId}</p>
							</div>
							<div class='row'>
								<h5>Client Secret</h5>
								<p>${iter.clientSecret}</p>
							</div>
							`;
		const uriDiv= document.createElement('uriDiv');
		uriDiv.innerHTML=`
							<h5>Redirect Uri(s)</h5>
							`;
		var index=0;
		const uriList= iter.redirectUri;
		
		uriList.forEach(row => {
			const uri= document.createElement('div');
			uri.classList.add('row');
			uri.innerHTML=`
							<h5>${++index}</h5>
							<p>${row}</p>
							`
							
			uriDiv.appendChild(uri);
		});
		element.appendChild(uriDiv);
		container.appendChild(element);
	});
}

function logout()
{
	window.location.href= "/OurAuth/account?type=logout";
}

window.onload= getData();