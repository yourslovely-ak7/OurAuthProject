const box= document.getElementById("uriDiv");

function getUrls()
{
	let urls = '';
		const inputs = document.querySelectorAll('input[type="url"]');

		inputs.forEach(input => {
			if (input.value.trim() !== '') {
				urls += input.value.trim() + ' ';
			}
		});

		console.log(urls);
		return urls;
}

async function registerClient()
{
	const name= document.getElementById('name').value;
	const url= getUrls();
	
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

function appendField()
{
	const element= document.createElement('div')
	element.classList.add('uriEle');
	element.innerHTML=`
						<input type="url">
						<button onclick= "deleteElement(this)">X</button>
						`;
	box.appendChild(element);
}

function deleteElement(button)
{
	console.log("Deleting element!")
	box.removeChild(button.parentElement);
}

function displayData(data)
{
	document.getElementById('firstBox').style.display= 'none';
	document.getElementById('secondBox').style.display= 'block';
	
	document.getElementById('cName').value= data.name;
	document.getElementById('cId').value= data.clientId;
	document.getElementById('cSecret').value= data.clientSecret;
}