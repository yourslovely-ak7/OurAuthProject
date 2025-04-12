const container= document.getElementById("container");
const urlParam= new URLSearchParams(window.location.search);
const scopesArr= [];

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
		var row;
		
		if(iter.includes("."))
		{
			const box= document.createElement('input');
			box.type='checkbox';
			box.id= iter;
			box.value= iter;
			box.onchange= () => handleChange(box);
			container.appendChild(box);
			
			row= document.createElement('label');
			row.htmlFor= iter;
		}
		else
		{			
			row= document.createElement('h4');
			scopesArr.push(iter);
		}
		
		row.textContent= `${index}) ${iter}`;
		container.appendChild(row);
		index++;
	})
}

function handleChange(box)
{
	const value= box.value;
	if(box.checked)
	{
		if(!scopesArr.includes(value))
		{
			scopesArr.push(value);
		}
	}
	else
	{
		const index= scopesArr.indexOf(value);
		if(index > -1)
		{
			scopesArr.splice(index, 1);
		}
	}
	
	console.log('Values: '+scopesArr.join(" "));
}

function proceed()
{
	urlParam.delete('responseType');
	urlParam.delete('serviceUrl');
	window.location.href= `/OurAuth/auth?response_type=consent&${urlParam.toString()}&agreed_scopes=${scopesArr.join(" ")}`;
}

function reject()
{
	const url= urlParam.get('redirectUrl');
	window.location.href= `${url}?error=access_declined`;
}