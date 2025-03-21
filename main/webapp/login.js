async function loginUser()
{
	const email= document.getElementById('email').value;
	const password= document.getElementById('password').value;
	
	const urlParam= new URLSearchParams(window.location.search);
	console.log(urlParam.toString());
	const serviceUrl= urlParam.get("serviceUrl");
	urlParam.delete('serviceUrl');
	const remainingParams= urlParam.toString();
	
	const response= await fetch(`/OurAuth/account?type=login&serviceUrl=${encodeURIComponent(serviceUrl)}`,{
				method: 'POST',
				body: JSON.stringify({email, password})
			});
			
		if(response.ok)
		{
			const data= await response.json();
			window.location.href= `${data.serviceUrl}?${remainingParams}`;
		}
		else
		{
			alert('Invalid Credentials!');
		}
}