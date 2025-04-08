const container= document.getElementById("container");
const urlParam= new URLSearchParams(window.location.search);

window.onload= retrieveData();

function retrieveData()
{
	const name= urlParam.get("name");
	const element= document.createElement('h2');
	element.textContent=`Would you like to allow ${name} App to access the following?`;
	container.appendChild(element);
	
	const scopeParam= urlParam.get("scopes");
	console.log(scopeParam);
	
	const scopes= scopeParam.split(" ");
	var index= 1;
	scopes.forEach(iter => {
		
		const row= document.createElement('h4');
		row.textContent= `${index}) ${iter}`;
		container.appendChild(row);
		index++;
	})
}

function proceed()
{
	urlParam.delete('responseType');
	urlParam.delete('serviceUrl');
	window.location.href= `/OurAuth/auth?responseType=consent&${urlParam.toString()}`;
}

function reject()
{
	const url= urlParam.get('redirectUrl');
	window.location.href= `${url}?error=access_declined`;
}