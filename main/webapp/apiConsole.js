
async function registerClient()
{
	const name= document.getElementById('name').value;
	const url= document.getElementById('url').value;
	
	const response= await fetch(`/OurAuth/client?name=${name}&redirectUrl=${url}`,{
		method: 'POST',
	});
	
	if(response.ok)
	{
		const data= await response.json();
		displayData(data);
	}
	else
	{
		alert('Error occurred while registering client!');
	}
}

function displayData(data)
{
	document.getElementById('firstBox').style.display= 'none';
	document.getElementById('secondBox').style.display= 'block';
	
	document.getElementById('cName').value= data.name;
	document.getElementById('cId').value= data.clientId;
	document.getElementById('cSecret').value= data.clientSecret;
}